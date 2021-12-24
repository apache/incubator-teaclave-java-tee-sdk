package com.alibaba.confidentialcomputing.host.exception;

/**
 * ConfidentialComputingException {@link ConfidentialComputingException} is base exception in
 * JavaEnclave's host. All exceptions thrown in JavaEnclave host will inherit this
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
}