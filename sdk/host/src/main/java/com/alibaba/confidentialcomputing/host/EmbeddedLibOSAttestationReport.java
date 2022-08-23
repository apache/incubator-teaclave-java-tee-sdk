package com.alibaba.confidentialcomputing.host;

/**
 * EmbeddedLibOSAttestationReport includes more detail information of remote attestation report
 * for lib_os_embedded type enclave.
 */
final class EmbeddedLibOSAttestationReport extends SGXAttestationReport {
    EmbeddedLibOSAttestationReport(byte[] quote, byte[] mrSigner, byte[] mrEnclave, byte[] userData) {
        super(EnclaveType.EMBEDDED_LIB_OS, quote, mrSigner, mrEnclave, userData);
    }
}
