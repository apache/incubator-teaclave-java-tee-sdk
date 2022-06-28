package com.alibaba.confidentialcomputing.enclave.agent;

import com.alibaba.confidentialcomputing.common.EmbeddedLibOSInnerAttestationReport;
import com.alibaba.confidentialcomputing.common.exception.ConfidentialComputingException;

public class RemoteAttestation {
    // lib os embedded enclave remote attestation jni.so path in occlum image.
    private final static String JNI_EXTRACTED_PACKAGE_PATH = "/usr/lib/libos_occlum_enclave_attestation/lib_occlum_attestation_generate.so";

    private static native void registerNatives();
    private static native EmbeddedLibOSInnerAttestationReport generateAttestationReportNative(byte[] userDate) throws ConfidentialComputingException;

    static {
        System.load(JNI_EXTRACTED_PACKAGE_PATH);
        registerNatives();
    }

    public static EmbeddedLibOSInnerAttestationReport generateAttestationReport(byte[] userDate) throws ConfidentialComputingException {
        return generateAttestationReportNative(userDate);
    }
}
