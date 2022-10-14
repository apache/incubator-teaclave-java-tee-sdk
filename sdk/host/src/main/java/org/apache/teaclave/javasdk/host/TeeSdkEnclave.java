// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.teaclave.javasdk.host;

import org.apache.teaclave.javasdk.common.EnclaveInvocationContext;
import org.apache.teaclave.javasdk.common.SerializationHelper;
import org.apache.teaclave.javasdk.common.ServiceHandler;
import org.apache.teaclave.javasdk.host.exception.*;

import java.io.IOException;

/**
 * TeeSdkEnclave is a sgx2 enclave based on Intel's sgx sdk.
 */
final class TeeSdkEnclave extends AbstractEnclave {
    private final static long KB = 1024;
    private final static long MB = KB * 1024;
    private final static String JNI_EXTRACTED_PACKAGE_PATH = "jni/lib_jni_tee_sdk_svm.so";
    private final static String TEE_SDK_SIGNED_PACKAGE_PATH = "lib_tee_sdk_svm_load.signed";
    private static volatile TeeSdkExtractTempPath extractTempPath;

    // enclaveHandle stores created enclave's handle id.
    private long enclaveHandle;
    // isolate stores svm created isolate instance.
    // In Teaclave Java TEE SDK only one isolateHandle instance will be created.
    private long isolateHandle;
    // isolateThreadHandle stores the first attached isolateThread Handle.
    private long isolateThreadHandle;
    private final SGXEnclaveInfo enclaveInfo;

    private void extractNativeResource() throws EnclaveCreatingException {
        // Extract jni .so and signed tee .so from .jar file.
        // Only once extract and load operation.
        if (extractTempPath == null) {
            synchronized (TeeSdkEnclave.class) {
                if (extractTempPath == null) {
                    try {
                        String jniTempFilePath = ExtractLibrary.extractLibrary(
                                TeeSdkEnclave.class.getClassLoader(),
                                JNI_EXTRACTED_PACKAGE_PATH);
                        String teeSdkSignedFilePath = ExtractLibrary.extractLibrary(
                                TeeSdkEnclave.class.getClassLoader(),
                                TEE_SDK_SIGNED_PACKAGE_PATH);
                        extractTempPath = new TeeSdkExtractTempPath(jniTempFilePath, teeSdkSignedFilePath);
                        System.load(extractTempPath.getJniTempFilePath());
                        registerNatives();
                    } catch (IOException e) {
                        throw new EnclaveCreatingException("extracting tee sdk jni .so or signed .so failed.", e);
                    }
                }
            }
        }
    }

    private String buildSVMHeapConf() throws IOException {
        long enclaveMaxHeapSize = TeeSdkEnclaveConfigure.getInstance().getEnclaveSVMMaxHeapSize();
        if ( enclaveMaxHeapSize > 0) {
            long size = enclaveMaxHeapSize / MB;
            if (size == 0) size = 1;
            return "-Xmx" + size + "m";
        }
        return "-Xmx" + 0 + "m";
    }

    TeeSdkEnclave(EnclaveDebug mode) throws EnclaveCreatingException {
        // Set EnclaveContext for this enclave instance.
        super(EnclaveType.TEE_SDK, mode, new EnclaveServicesRecycler());
        extractNativeResource();
        // Create tee sdk enclave by native call, enclaveHandler is set in jni in nativeHandlerContext.
        nativeCreateEnclave(mode.getValue(), extractTempPath.getTeeSdkSignedFilePath());
        try {
            // Create svm attach isolate and isolateThread, and they are set in jni in nativeHandlerContext.
            nativeSvmAttachIsolate(enclaveHandle, TeeSdkEnclaveConfigure.getInstance().isEnableTeeSDKSymbolTracing(), buildSVMHeapConf());
            // Create enclave info.
            boolean isDebuggable = mode.getValue() != 0x2;
            enclaveInfo = new SGXEnclaveInfo(
                    EnclaveType.TEE_SDK,
                    isDebuggable,
                    TeeSdkEnclaveConfigure.getInstance().getMaxEnclaveEPCMemorySizeBytes(),
                    TeeSdkEnclaveConfigure.getInstance().getMaxEnclaveThreadNum());
        } catch (IOException e) {
            throw new EnclaveCreatingException(e);
        }
    }

    private static native void registerNatives();

    private native int nativeCreateEnclave(int mode, String path) throws EnclaveCreatingException;

    private native TeeSdkAttestationReport nativeGenerateAttestationReport(long enclaveHandler, byte[] userData) throws RemoteAttestationException;

    private native int nativeSvmAttachIsolate(long enclaveHandler, int flag, String args) throws EnclaveCreatingException;

    private native byte[] nativeLoadService(long enclaveHandler, long isolateHandler, byte[] serviceHandler) throws ServicesLoadingException;

    private native byte[] nativeInvokeMethod(long enclaveHandler, long isolateHandler, byte[] enclaveInvokeMetaWrapper) throws EnclaveMethodInvokingException;

    private native byte[] nativeUnloadService(long enclaveHandler, long isolateHandler, byte[] serviceHandler) throws ServicesUnloadingException;

    private native int nativeSvmDetachIsolate(long enclaveHandler, long isolateThreadHandler) throws EnclaveDestroyingException;

    private native int nativeDestroyEnclave(long enclaveHandler) throws EnclaveDestroyingException;

    static int verifyAttestationReport(byte[] quote) throws RemoteAttestationException {
        return SGXRemoteAttestationVerify.VerifyAttestationReport(quote);
    }

    @Override
    byte[] loadServiceNative(String service) throws ServicesLoadingException {
        byte[] payload;
        try {
            payload = SerializationHelper.serialize(service);
        } catch (IOException e) {
            throw new ServicesLoadingException("service name serialization failed.", e);
        }
        return nativeLoadService(enclaveHandle, isolateHandle, payload);
    }

    @Override
    byte[] unloadServiceNative(ServiceHandler handler) throws ServicesUnloadingException {
        byte[] payload;
        try {
            payload = SerializationHelper.serialize(handler);
        } catch (IOException e) {
            throw new ServicesUnloadingException("unload service serialization failed.", e);
        }
        return nativeUnloadService(enclaveHandle, isolateHandle, payload);
    }

    @Override
    byte[] invokeMethodNative(EnclaveInvocationContext context) throws EnclaveMethodInvokingException {
        byte[] payload;
        try {
            payload = SerializationHelper.serialize(context);
        } catch (IOException e) {
            throw new EnclaveMethodInvokingException("EnclaveInvokeMetaWrapper serialization failed.", e);
        }
        return nativeInvokeMethod(enclaveHandle, isolateHandle, payload);
    }

    @Override
    AttestationReport generateAttestationReportNative(byte[] userData) throws RemoteAttestationException {
        return nativeGenerateAttestationReport(enclaveHandle, userData);
    }

    @Override
    public EnclaveInfo getEnclaveInfo() {
        return this.enclaveInfo;
    }

    @Override
    public void destroy() throws EnclaveDestroyingException {
        // destroyToken will wait for all ongoing enclave invocations finished.
        if (this.getEnclaveContext().getEnclaveToken().destroyToken()) {
            try (MetricTraceContext trace = new MetricTraceContext(
                    this.getEnclaveInfo(),
                    MetricTraceContext.LogPrefix.METRIC_LOG_ENCLAVE_DESTROYING_PATTERN)) {
                // interrupt enclave services' recycler firstly.
                this.getEnclaveContext().getEnclaveServicesRecycler().interruptServiceRecycler();
                // destroy svm isolate.
                nativeSvmDetachIsolate(enclaveHandle, isolateThreadHandle);
                // destroy the enclave.
                nativeDestroyEnclave(enclaveHandle);
                EnclaveInfoManager.getEnclaveInfoManagerInstance().removeEnclave(this);
            } catch (MetricTraceLogWriteException e) {
                throw new EnclaveDestroyingException(e);
            }
        }
    }

    static class TeeSdkExtractTempPath {
        private final String jniTempFilePath;
        private final String teeSdkSignedFilePath;

        TeeSdkExtractTempPath(String jniTempFilePath, String teeSdkSignedFilePath) {
            this.jniTempFilePath = jniTempFilePath;
            this.teeSdkSignedFilePath = teeSdkSignedFilePath;
        }

        String getJniTempFilePath() {
            return jniTempFilePath;
        }

        String getTeeSdkSignedFilePath() {
            return teeSdkSignedFilePath;
        }
    }
}
