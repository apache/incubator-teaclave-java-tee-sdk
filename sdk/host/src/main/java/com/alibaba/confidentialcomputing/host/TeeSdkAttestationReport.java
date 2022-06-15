package com.alibaba.confidentialcomputing.host;

/**
 * TeeSdkAttestationReport parse more details information from a tee sdk type enclave's remote attestation report.
 */
public final class TeeSdkAttestationReport extends AttestationReport {
    private final byte[] mrSigner;
    private final byte[] mrEnclave;

    TeeSdkAttestationReport(byte[] quote, byte[] mrSigner, byte[] mrEnclave) {
        super(EnclaveType.TEE_SDK, quote);
        this.mrSigner = mrSigner;
        this.mrEnclave = mrEnclave;
    }

    /**
     * Get enclave measurementEnclave from an enclave's remote attestation report.
     * <p>
     *
     * @return Remote attestation measurementEnclave value.
     */
    public byte[] getMeasurementEnclave() {
        return this.mrEnclave;
    }

    /**
     * Get enclave measurementSigner from an enclave's remote attestation report.
     * <p>
     *
     * @return Remote attestation measurementSigner value.
     */
    public byte[] getMeasurementSigner() {
        return this.mrSigner;
    }
}
