package com.alibaba.confidentialcomputing.host;

import com.alibaba.confidentialcomputing.host.exception.RemoteAttestationException;

import java.io.IOException;

final class SGXRemoteAttestationVerify {
    private final static String JNI_EXTRACTED_PACKAGE_PATH = "remote_attestation/sgx/jni/lib_jni_sgx_remote_attestation_verify.so";

    static {
        try {
            String jniTempFilePath = ExtractLibrary.extractLibrary(SGXRemoteAttestationVerify.class.getClassLoader(),
                    JNI_EXTRACTED_PACKAGE_PATH);
            System.load(jniTempFilePath);
            registerNatives();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static native void registerNatives();
    private static native int nativeVerifyAttestationReport(byte[] report, RemoteAttestationVerifyResult result);

    static int VerifyAttestationReport(byte[] report) throws RemoteAttestationException {
        RemoteAttestationVerifyResult verifyResult = new RemoteAttestationVerifyResult(0, 0, 0);
        nativeVerifyAttestationReport(report, verifyResult);
        if (verifyResult.getVersionCheck() == -1) {
            throw new RemoteAttestationException("sgx_qv_get_quote_supplemental_data_size returned size is not same with header definition in SGX SDK");
        } else if (verifyResult.getStatus() == 1) {
            throw new RemoteAttestationException("sgx_qv_get_quote_supplemental_data_size failed");
        } else if (verifyResult.getStatus() == 2) {
            throw new RemoteAttestationException("sgx_qv_verify_quote failed");
        } else if (verifyResult.getStatus() == 3) {
            throw new RemoteAttestationException("supplemental data memory allocation failed");
        } else if (verifyResult.getVerifyFlag() == 1) {
            throw new RemoteAttestationException("verification completed, but collateral is out of date");
        } else if (verifyResult.getVerifyFlag() == 2) {
            throw new RemoteAttestationException("verification completed with non-terminal result");
        } else if (verifyResult.getVerifyFlag() == 3) {
            throw new RemoteAttestationException("verification completed with terminal result, but verification check failed");
        } else {
            return verifyResult.getVerifyFlag();
        }
    }
}
