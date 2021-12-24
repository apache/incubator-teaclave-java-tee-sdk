package com.alibaba.confidentialcomputing.host.exception;

/**
 * ServicesLoadingException {@link ServicesLoadingException} is thrown when exception happen
 * during an enclave's service was loading.
 * Programmers need to handle ServicesLoadingException seriously.
 */
public class ServicesLoadingException extends ConfidentialComputingException {
    /**
     * @param info exception information.
     */
    public ServicesLoadingException(String info) {
        super(info);
    }

    /**
     * @param e exception.
     */
    public ServicesLoadingException(Throwable e) {
        super(e);
    }
}