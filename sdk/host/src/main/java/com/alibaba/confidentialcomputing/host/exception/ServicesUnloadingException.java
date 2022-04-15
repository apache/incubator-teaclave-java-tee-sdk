package com.alibaba.confidentialcomputing.host.exception;

import com.alibaba.confidentialcomputing.common.exception.ConfidentialComputingException;

/**
 * ServicesUnloadingException {@link ServicesUnloadingException} is thrown when exception happen
 * during an enclave's service was unloading.
 * Programmers need to handle UnloadServiceException seriously.
 */
public class ServicesUnloadingException extends ConfidentialComputingException {
    /**
     * @param info exception information.
     */
    public ServicesUnloadingException(String info) {
        super(EnclaveNativeInvokingException.SERVICES_UNLOADING_ERROR.buildExceptionMessage(info));
    }

    /**
     * @param e exception.
     */
    public ServicesUnloadingException(Throwable e) {
        super(EnclaveNativeInvokingException.SERVICES_UNLOADING_ERROR.toString(), e);
    }

    /**
     * @param info exception info.
     * @param e    exception.
     */
    public ServicesUnloadingException(String info, Throwable e) {
        super(EnclaveNativeInvokingException.SERVICES_UNLOADING_ERROR.buildExceptionMessage(info), e);
    }
}
