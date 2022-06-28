package com.alibaba.confidentialcomputing.common.exception;

/**
 * ConfidentialComputingException {@link ConfidentialComputingException} is base exception in
 * JavaEnclave. All exceptions thrown in JavaEnclave will inherit this base exception.
 * Programmers need to handle ConfidentialComputingException seriously.
 */
public class ConfidentialComputingException extends Exception {

    private static final long serialVersionUID = 5964126736764332957L;

    public ConfidentialComputingException() {super();}

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