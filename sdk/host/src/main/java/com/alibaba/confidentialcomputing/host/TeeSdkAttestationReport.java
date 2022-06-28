package com.alibaba.confidentialcomputing.host;

/**
 * TeeSdkAttestationReport parse more details information from a tee sdk type enclave's remote attestation report.
 */
public final class TeeSdkAttestationReport extends SGXAttestationReport {
    TeeSdkAttestationReport(byte[] quote, byte[] mrSigner, byte[] mrEnclave, byte[] userData) {
        super(EnclaveType.TEE_SDK, quote, mrSigner, mrEnclave, userData);
    }
}
