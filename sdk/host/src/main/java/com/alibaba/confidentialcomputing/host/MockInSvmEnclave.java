package com.alibaba.confidentialcomputing.host;

import com.alibaba.confidentialcomputing.host.exception.EnclaveCreatingException;
import com.alibaba.confidentialcomputing.host.exception.EnclaveDestroyingException;

import java.io.IOException;

/**
 * MockInSvmEnclave is a mock svm enclave. Host part code runs in jvm and enclave
 * part code was compiled into native image, we could view enclave part as a service
 * provider, and all service invocation will be implemented in enclave by reflection.
 * The work mechanism in this mode is very closed to tee sdk enclave, so it's very
 * important to debug issue.
 */
class MockInSvmEnclave extends AbstractEnclave {
    private final static String JNI_EXTRACTED_PACKAGE_PATH = "jni/lib_jni_mock_svm.so";
    private final static String ENCLAVE_SVM_WRAPPER_PACKAGE_PATH = "libs/lib_enclave_mock_svm_wrapper.so";
    private final static String ENCLAVE_SVM_PACKAGE_PATH = "libs/lib_svm_sdk.so";
    private static volatile MockInSvmExtractTempPath extractTempPath;
    private final EnclaveNativeContextCache nativeHandlerContext = new EnclaveNativeContextCache(
            0, 0, 0, 0);

    MockInSvmEnclave() throws EnclaveCreatingException {
        // Set EnclaveContext for this enclave instance.
        super(EnclaveType.MOCK_IN_SVM, new EnclaveServicesRecycler());

        // Extract jni .so and svm sdk .so from .jar file.
        if (extractTempPath == null) {
            synchronized (MockInSvmEnclave.class) {
                if (extractTempPath == null) {
                    try {
                        String jniTempFilePath = ExtractLibrary.extractLibrary(
                                MockInSvmEnclave.class.getClassLoader(),
                                JNI_EXTRACTED_PACKAGE_PATH);
                        String enclaveWrapperFilePath = ExtractLibrary.extractLibrary(
                                MockInSvmEnclave.class.getClassLoader(),
                                ENCLAVE_SVM_WRAPPER_PACKAGE_PATH);
                        String enclaveSvmFilePath = ExtractLibrary.extractLibrary(
                                MockInSvmEnclave.class.getClassLoader(),
                                ENCLAVE_SVM_PACKAGE_PATH);
                        extractTempPath = new MockInSvmEnclave.MockInSvmExtractTempPath(
                                jniTempFilePath,
                                enclaveWrapperFilePath,
                                enclaveSvmFilePath);
                        System.load(jniTempFilePath);
                    } catch (IOException e) {
                        throw new EnclaveCreatingException("extracting tee sdk jni .so or signed .so failed.", e);
                    }
                }
            }
        }

        // Create svm sdk enclave by native call, enclaveWrapperHandle and enclaveSvmSdkHandle are set in jni in nativeHandlerContext.
        int ret = nativeCreateEnclave(extractTempPath.getJniTempFilePath());
        if (ret != 0) {
            throw new EnclaveCreatingException("create svm sdk enclave by native calling failed.");
        }
        // Create svm attach isolate and isolateThread, and they are set in jni in nativeHandlerContext.
        ret = nativeSvmAttachIsolate(nativeHandlerContext.getEnclaveWrapperHandle(),
                nativeHandlerContext.getEnclaveSvmSdkHandle());
        if (ret != 0) {
            throw new EnclaveCreatingException("create svm isolate by native calling failed.");
        }
    }

    @Override
    InnerNativeInvocationResult loadServiceNative(byte[] payload) {
        return nativeLoadService(
                nativeHandlerContext.getEnclaveWrapperHandle(),
                nativeHandlerContext.getEnclaveSvmSdkHandle(),
                nativeHandlerContext.getIsolateHandle(),
                payload);
    }

    @Override
    InnerNativeInvocationResult unloadServiceNative(byte[] payload) {
        return nativeUnloadService(
                nativeHandlerContext.getEnclaveWrapperHandle(),
                nativeHandlerContext.getEnclaveSvmSdkHandle(),
                nativeHandlerContext.getIsolateHandle(),
                payload);
    }

    @Override
    InnerNativeInvocationResult invokeMethodNative(byte[] payload) {
        return nativeInvokeMethod(
                nativeHandlerContext.getEnclaveWrapperHandle(),
                nativeHandlerContext.getEnclaveSvmSdkHandle(),
                nativeHandlerContext.getIsolateHandle(),
                payload);
    }

    @Override
    public void destroy() throws EnclaveDestroyingException {
        // destroyToken will wait for all ongoing enclave invocations finished.
        if (this.getEnclaveContext().getEnclaveToken().destroyToken()) {
            // interrupt enclave services' recycler firstly.
            this.getEnclaveContext().getEnclaveServicesRecycler().interruptServiceRecycler();
            // destroy svm isolate.
            int ret = nativeSvmDetachIsolate(
                    nativeHandlerContext.getEnclaveWrapperHandle(),
                    nativeHandlerContext.getEnclaveSvmSdkHandle(),
                    nativeHandlerContext.getIsolateThreadHandle());
            if (ret != 0) {
                throw new EnclaveDestroyingException("isolate destroy native call failed.");
            }
            ret = nativeDestroyEnclave(
                    nativeHandlerContext.getEnclaveWrapperHandle(),
                    nativeHandlerContext.getEnclaveSvmSdkHandle());
            if (ret != 0) {
                throw new EnclaveDestroyingException("enclave destroy native call failed.");
            }
        }

    }

    private native int nativeCreateEnclave(String path);

    private native int nativeSvmAttachIsolate(
            long enclaveWrapperHandle,
            long enclaveSvmSdkHandle);

    private native InnerNativeInvocationResult nativeLoadService(
            long enclaveWrapperHandle,
            long enclaveSvmSdkHandle,
            long isolateHandler,
            byte[] serviceHandler);

    private native InnerNativeInvocationResult nativeInvokeMethod(
            long enclaveWrapperHandle,
            long enclaveSvmSdkHandle,
            long isolateHandler,
            byte[] enclaveInvokeMetaWrapper);

    private native InnerNativeInvocationResult nativeUnloadService(
            long enclaveWrapperHandle,
            long enclaveSvmSdkHandle,
            long isolateHandler,
            byte[] serviceHandler);

    private native int nativeSvmDetachIsolate(
            long enclaveWrapperHandle,
            long enclaveSvmSdkHandle,
            long isolateThreadHandler);

    private native int nativeDestroyEnclave(
            long enclaveWrapperHandle,
            long enclaveSvmSdkHandle);

    /**
     * JavaEnclave will create svm isolate handle and isolateThread handle by native call,
     * so EnclaveNativeContextCache will cache them for usage.
     */
    class EnclaveNativeContextCache {
        // enclaveHandle stores created enclave wrap .so file handler.
        private final long enclaveWrapperHandle;
        // enclaveHandle stores created enclave svm sdk .so file handler.
        private final long enclaveSvmSdkHandle;
        // isolate stores svm created isolate instance.
        // In JavaEnclave only one isolateHandle instance will be created.
        private final long isolateHandle;
        // isolateThreadHandle stores the first attached isolateThread Handle.
        private final long isolateThreadHandle;

        EnclaveNativeContextCache(
                long enclaveWrapperHandle, long enclaveSvmSdkHandle,
                long isolateHandle, long isolateThreadHandle) {
            this.enclaveWrapperHandle = enclaveWrapperHandle;
            this.enclaveSvmSdkHandle = enclaveSvmSdkHandle;
            this.isolateHandle = isolateHandle;
            this.isolateThreadHandle = isolateThreadHandle;
        }

        long getEnclaveWrapperHandle() {
            return enclaveWrapperHandle;
        }

        long getEnclaveSvmSdkHandle() {
            return enclaveSvmSdkHandle;
        }

        long getIsolateHandle() {
            return isolateHandle;
        }

        long getIsolateThreadHandle() {
            return isolateThreadHandle;
        }
    }

    class MockInSvmExtractTempPath {
        private final String jniTempFilePath;
        private final String enclaveWrapperFilePath;
        private final String enclaveSvmFilePath;

        MockInSvmExtractTempPath(String jniTempFilePath, String enclaveWrapperFilePath, String enclaveSvmFilePath) {
            this.jniTempFilePath = jniTempFilePath;
            this.enclaveWrapperFilePath = enclaveWrapperFilePath;
            this.enclaveSvmFilePath = enclaveSvmFilePath;
        }

        String getJniTempFilePath() {
            return jniTempFilePath;
        }

        String getEnclaveWrapperFilePath() {
            return enclaveWrapperFilePath;
        }

        String getEnclaveSvmFilePath() {
            return enclaveSvmFilePath;
        }
    }
}
