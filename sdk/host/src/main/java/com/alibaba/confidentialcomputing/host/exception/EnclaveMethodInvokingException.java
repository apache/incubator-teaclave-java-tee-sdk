package com.alibaba.confidentialcomputing.host.exception;

import com.alibaba.confidentialcomputing.common.exception.ConfidentialComputingException;

/**
 * EnclaveMethodInvokingException {@link EnclaveMethodInvokingException} is thrown when exception happen
 * during an enclave's method was invoking.
 * Programmers need to handle EnclaveInvokeException seriously.
 */
public class EnclaveMethodInvokingException extends ConfidentialComputingException {
    /**
     * @param info exception information.
     */
    public EnclaveMethodInvokingException(String info) {
        super(EnclaveNativeInvokingException.SERVICE_METHOD_INVOKING_ERROR.buildExceptionMessage(info));
    }

    /**
     * @param e exception.
     */
    public EnclaveMethodInvokingException(Throwable e) {
        super(EnclaveNativeInvokingException.SERVICE_METHOD_INVOKING_ERROR.toString(), e);
    }

    /**
     * @param info exception message.
     * @param e    exception.
     */
    public EnclaveMethodInvokingException(String info, Throwable e) {
        super(EnclaveNativeInvokingException.SERVICE_METHOD_INVOKING_ERROR.buildExceptionMessage(info), e);
    }
}
