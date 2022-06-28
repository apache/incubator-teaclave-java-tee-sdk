package com.alibaba.confidentialcomputing.host;

import java.io.Serializable;

/**
 * AttestationReport wraps enclave's type and generated remote attestation report.
 */
public class AttestationReport implements Serializable {
    private static final long serialVersionUID = -2781780414647128479L;

    private final EnclaveType enclaveType;
    private final byte[] quote;

    AttestationReport(EnclaveType enclaveType, byte[] quote) {
        this.enclaveType = enclaveType;
        this.quote = quote;
    }

    /**
     * Get enclave type from an AttestationReport instance.
     * <p>
     *
     * @return Enclave type.
     */
    public EnclaveType getEnclaveType() {
        return enclaveType;
    }

    /**
     * Get enclave quote from an AttestationReport instance.
     * <p>
     *
     * @return Remote attestation quote data.
     */
    public byte[] getQuote() {
        return quote;
    }

    /**
     * Bind an AttestationReport's type and quote into a buffer for rpc transmission.
     * <p>
     *
     * @return Serialized buffer.
     */
    public byte[] toByteArray() {
        byte[] bindReport = new byte[1 + quote.length];
        bindReport[0] = (byte) enclaveType.ordinal();
        System.arraycopy(quote, 0, bindReport, 1, quote.length);
        return bindReport;
    }

    /**
     * Build an AttestationReport instance from a bind buffer which contains its type and report.
     * <p>
     *
     * @return AttestationReport instance.
     */
    public static AttestationReport fromByteArray(byte[] attestationReport) {
        EnclaveType enclaveType = EnclaveType.NONE;
        byte[] report = new byte[attestationReport.length - 1];
        switch (attestationReport[0]) {
            case 0:
                enclaveType = EnclaveType.NONE;
                break;
            case 1:
                enclaveType = EnclaveType.MOCK_IN_JVM;
                break;
            case 2:
                enclaveType = EnclaveType.MOCK_IN_SVM;
                break;
            case 3:
                enclaveType = EnclaveType.TEE_SDK;
                break;
            case 4:
                enclaveType = EnclaveType.EMBEDDED_LIB_OS;
                break;
        }
        System.arraycopy(attestationReport, 1, report, 0, report.length);
        return new AttestationReport(enclaveType, report);
    }
}
