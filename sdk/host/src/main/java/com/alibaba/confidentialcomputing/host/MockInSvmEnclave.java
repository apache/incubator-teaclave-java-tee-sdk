package com.alibaba.confidentialcomputing.host;

import com.alibaba.confidentialcomputing.common.EnclaveInvocationContext;
import com.alibaba.confidentialcomputing.common.SerializationHelper;
import com.alibaba.confidentialcomputing.common.ServiceHandler;
import com.alibaba.confidentialcomputing.host.exception.*;

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
    private final static String ENCLAVE_SVM_PACKAGE_PATH = "lib_mock_svm_load.so";
    private static volatile MockInSvmExtractTempPath extractTempPath;

    // enclaveHandle stores created enclave svm sdk .so file handler.
    private long enclaveSvmSdkHandle;
    // isolate stores svm created isolate instance.
    // In JavaEnclave only one isolateHandle instance will be created.
    private long isolateHandle;
    // isolateThreadHandle stores the first attached isolateThread Handle.
    private long isolateThreadHandle;

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
                        String enclaveSvmFilePath = ExtractLibrary.extractLibrary(
                                MockInSvmEnclave.class.getClassLoader(),
                                ENCLAVE_SVM_PACKAGE_PATH);
                        extractTempPath = new MockInSvmEnclave.MockInSvmExtractTempPath(
                                jniTempFilePath,
                                enclaveSvmFilePath);
                        System.load(extractTempPath.getJniTempFilePath());
                        registerNatives();
                    } catch (IOException e) {
                        throw new EnclaveCreatingException("extracting tee sdk jni .so or signed .so failed.", e);
                    }
                }
            }
        }

        // Create svm sdk enclave by native call, enclaveSvmSdkHandle are set in jni in nativeHandlerContext.
        nativeCreateEnclave(extractTempPath.getEnclaveSvmFilePath());
        // Create svm attach isolate and isolateThread, and they are set in jni in nativeHandlerContext.
        nativeSvmAttachIsolate(enclaveSvmSdkHandle);
    }

    @Override
    AttestationReport generateAttestationReportNative(byte[] userData) throws RemoteAttestationException {
        throw new RemoteAttestationException("MOCK_IN_SVM enclave doesn't support remote attestation generation.");
    }

    static int verifyAttestationReport(byte[] report) throws RemoteAttestationException {
        throw new RemoteAttestationException("MOCK_IN_SVM enclave doesn't support remote attestation verification.");
    }

    @Override
    byte[] loadServiceNative(String service) throws ServicesLoadingException {
        byte[] payload;
        try {
            payload = SerializationHelper.serialize(service);
        } catch (IOException e) {
            throw new ServicesLoadingException("service name serialization failed.", e);
        }
        return nativeLoadService(enclaveSvmSdkHandle, isolateHandle, payload);
    }

    @Override
    byte[] unloadServiceNative(ServiceHandler handler) throws ServicesUnloadingException {
        byte[] payload;
        try {
            payload = SerializationHelper.serialize(handler);
        } catch (IOException e) {
            throw new ServicesUnloadingException("unload service serialization failed.", e);
        }
        return nativeUnloadService(enclaveSvmSdkHandle, isolateHandle, payload);
    }

    @Override
    byte[] invokeMethodNative(EnclaveInvocationContext context) throws EnclaveMethodInvokingException {
        byte[] payload;
        try {
            payload = SerializationHelper.serialize(context);
        } catch (IOException e) {
            throw new EnclaveMethodInvokingException("EnclaveInvokeMetaWrapper serialization failed.", e);
        }
        return nativeInvokeMethod(enclaveSvmSdkHandle, isolateHandle, payload);
    }

    @Override
    public void destroy() throws EnclaveDestroyingException {
        // destroyToken will wait for all ongoing enclave invocations finished.
        if (this.getEnclaveContext().getEnclaveToken().destroyToken()) {
            // interrupt enclave services' recycler firstly.
            this.getEnclaveContext().getEnclaveServicesRecycler().interruptServiceRecycler();
            // destroy svm isolate.
            nativeSvmDetachIsolate(enclaveSvmSdkHandle, isolateThreadHandle);
            nativeDestroyEnclave(enclaveSvmSdkHandle);
        }
    }

    private static native void registerNatives();

    private native int nativeCreateEnclave(String path) throws EnclaveCreatingException;

    private native int nativeSvmAttachIsolate(long enclaveSvmSdkHandle) throws EnclaveCreatingException;

    private native byte[] nativeLoadService(long enclaveSvmSdkHandle, long isolateHandler, byte[] serviceHandler) throws ServicesLoadingException;

    private native byte[] nativeInvokeMethod(long enclaveSvmSdkHandle, long isolateHandler, byte[] enclaveInvokeMetaWrapper) throws EnclaveMethodInvokingException;

    private native byte[] nativeUnloadService(long enclaveSvmSdkHandle, long isolateHandler, byte[] serviceHandler) throws ServicesUnloadingException;

    private native int nativeSvmDetachIsolate(long enclaveSvmSdkHandle, long isolateThreadHandler) throws EnclaveDestroyingException;

    private native int nativeDestroyEnclave(long enclaveSvmSdkHandle) throws EnclaveDestroyingException;

    class MockInSvmExtractTempPath {
        private final String jniTempFilePath;
        private final String enclaveSvmFilePath;

        MockInSvmExtractTempPath(String jniTempFilePath, String enclaveSvmFilePath) {
            this.jniTempFilePath = jniTempFilePath;
            this.enclaveSvmFilePath = enclaveSvmFilePath;
        }

        String getJniTempFilePath() {
            return jniTempFilePath;
        }

        String getEnclaveSvmFilePath() {
            return enclaveSvmFilePath;
        }
    }
}
