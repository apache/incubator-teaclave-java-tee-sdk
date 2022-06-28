package com.alibaba.confidentialcomputing.host;

/**
 * EmbeddedLibOSAttestationReport parse more details information from a lib os embedded type enclave's remote attestation report.
 */
public final class EmbeddedLibOSAttestationReport extends SGXAttestationReport {
    EmbeddedLibOSAttestationReport(byte[] quote, byte[] mrSigner, byte[] mrEnclave, byte[] userData) {
        super(EnclaveType.EMBEDDED_LIB_OS, quote, mrSigner, mrEnclave, userData);
    }
}
