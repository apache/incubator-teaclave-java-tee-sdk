package com.alibaba.confidentialcomputing.host;

/**
 * SGX type enclave's remote attestation report.
 */
public class SGXAttestationReport extends AttestationReport {
    private final byte[] mrSigner;
    private final byte[] mrEnclave;
    private final byte[] userData;

    SGXAttestationReport(EnclaveType type, byte[] quote, byte[] mrSigner, byte[] mrEnclave, byte[] userData) {
        super(type, quote);
        this.mrSigner = mrSigner;
        this.mrEnclave = mrEnclave;
        this.userData = userData;
    }

    /**
     * Get enclave userData from an enclave's remote attestation report.
     * <p>
     *
     * @return Remote attestation userData value which is from user.
     */
    public byte[] getUserData() {
        return this.userData;
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
