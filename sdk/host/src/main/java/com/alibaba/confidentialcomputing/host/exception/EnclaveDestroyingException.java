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
        super(EnclaveNativeInvokingException.ENCLAVE_DESTROYING_ERROR.buildExceptionMessage(info));
    }

    /**
     * @param e exception.
     */
    public EnclaveDestroyingException(Throwable e) {
        super(EnclaveNativeInvokingException.ENCLAVE_DESTROYING_ERROR.toString(), e);
    }

    /**
     * @param info exception message.
     * @param e    exception.
     */
    public EnclaveDestroyingException(String info, Throwable e) {
        super(EnclaveNativeInvokingException.ENCLAVE_DESTROYING_ERROR.buildExceptionMessage(info), e);
    }
}