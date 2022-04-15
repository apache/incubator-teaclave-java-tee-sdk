package com.alibaba.confidentialcomputing.host.exception;

import com.alibaba.confidentialcomputing.common.exception.ConfidentialComputingException;

/**
 * RemoteAttestationException {@link RemoteAttestationException} is thrown when an enclave generates remote
 * attestation report and returns an error value.
 * Programmers need to handle RemoteAttestationException seriously.
 */
public class RemoteAttestationException extends ConfidentialComputingException {
    /**
     * @param info exception information.
     */
    public RemoteAttestationException(String info) {
        super(EnclaveNativeInvokingException.ENCLAVE_REMOTE_ATTESTATION_ERROR.buildExceptionMessage(info));
    }

    /**
     * @param e exception.
     */
    public RemoteAttestationException(Throwable e) {
        super(EnclaveNativeInvokingException.ENCLAVE_REMOTE_ATTESTATION_ERROR.toString(), e);
    }

    /**
     * @param info exception message.
     * @param e    exception.
     */
    public RemoteAttestationException(String info, Throwable e) {
        super(EnclaveNativeInvokingException.ENCLAVE_REMOTE_ATTESTATION_ERROR.buildExceptionMessage(info), e);
    }
}
