package com.alibaba.confidentialcomputing.host.exception;

import com.alibaba.confidentialcomputing.common.exception.ConfidentialComputingException;

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
        super(EnclaveNativeInvokingException.SERVICES_LOADING_ERROR.buildExceptionMessage(info));
    }

    /**
     * @param e exception.
     */
    public ServicesLoadingException(Throwable e) {
        super(EnclaveNativeInvokingException.SERVICES_LOADING_ERROR.toString(), e);
    }

    /**
     * @param info exception info.
     * @param e    exception.
     */
    public ServicesLoadingException(String info, Throwable e) {
        super(EnclaveNativeInvokingException.SERVICES_LOADING_ERROR.buildExceptionMessage(info), e);
    }
}