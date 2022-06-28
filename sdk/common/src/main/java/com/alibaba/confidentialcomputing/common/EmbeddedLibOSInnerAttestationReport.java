package com.alibaba.confidentialcomputing.common;

import java.io.Serializable;

/**
 * This class is used to transfer embedded lib os attestation report between JavaEnclave's
 * host and enclave module.
 */
public final class EmbeddedLibOSInnerAttestationReport implements Serializable {
    private static final long serialVersionUID = -6944029051086666440L;

    private final byte[] quote;
    private final byte[] mrSigner;
    private final byte[] mrEnclave;
    private final byte[] userData;


    public EmbeddedLibOSInnerAttestationReport(byte[] quote, byte[] mrSigner, byte[] mrEnclave, byte[] userData) {
        this.quote = quote;
        this.mrSigner = mrSigner;
        this.mrEnclave = mrEnclave;
        this.userData = userData;
    }

    public byte[] getQuote() {
        return this.quote;
    }

    public byte[] getMrSigner() {
        return this.mrSigner;
    }

    public byte[] getMrEnclave() {
        return this.mrEnclave;
    }

    public byte[] getUserData() {
        return this.userData;
    }
}
