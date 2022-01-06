package com.alibaba.confidentialcomputing.enclave.exception;

/**
 * ConfidentialComputingException {@link ConfidentialComputingException} is base exception in
 * JavaEnclave's enclave. All exceptions thrown in JavaEnclave enclave will inherit this
 * base exception.
 * Programmers need to handle ConfidentialComputingException seriously.
 */
public class ConfidentialComputingException extends Exception {
    /**
     * @param info exception information.
     */
    public ConfidentialComputingException(String info) {
        super(info);
    }

    /**
     * @param e exception.
     */
    public ConfidentialComputingException(Throwable e) {
        super(e);
    }

    /**
     * @param info exception information.
     * @param e    exception.
     */
    public ConfidentialComputingException(String info, Throwable e) {
        super(info, e);
    }
}