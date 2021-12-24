package com.alibaba.confidentialcomputing.host.exception;

/**
 * EnclaveDestroyingException {@link EnclaveDestroyingException} is thrown when exception happen
 * during an enclave was destroying.
 * Programmers need to handle EnclaveDestroyingException seriously.
 */
public class EnclaveDestroyingException extends ConfidentialComputingException {
    /**
     * @param info exception information.
     */
    public EnclaveDestroyingException(String info) {
        super(info);
    }

    /**
     * @param e exception.
     */
    public EnclaveDestroyingException(Throwable e) {
        super(e);
    }
}