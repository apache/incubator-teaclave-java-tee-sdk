package com.alibaba.confidentialcomputing.host.exception;

/**
 * EnclaveCreatingException {@link EnclaveCreatingException} is thrown when exception happen
 * during an enclave was creating.
 * Programmers need to handle EnclaveCreatingException seriously.
 */
public class EnclaveCreatingException extends ConfidentialComputingException {
    /**
     * @param info exception information.
     */
    public EnclaveCreatingException(String info) {
        super(info);
    }

    /**
     * @param e exception.
     */
    public EnclaveCreatingException(Throwable e) {
        super(e);
    }
}