package com.alibaba.confidentialcomputing.host.exception;

/**
 * MetricTraceLogWriteException {@link MetricTraceLogWriteException} is thrown when an enclave metric trace
 * write into log file.
 * Programmers need to handle MetricTraceLogWriteException seriously.
 */
public class MetricTraceLogWriteException extends EnclaveCreatingException {
    /**
     * @param info exception information.
     */
    public MetricTraceLogWriteException(String info) {
        super(EnclaveNativeInvokingException.ENCLAVE_REMOTE_ATTESTATION_ERROR.buildExceptionMessage(info));
    }

    /**
     * @param e exception.
     */
    public MetricTraceLogWriteException(Throwable e) {
        super(EnclaveNativeInvokingException.ENCLAVE_REMOTE_ATTESTATION_ERROR.toString(), e);
    }

    /**
     * @param info exception message.
     * @param e    exception.
     */
    public MetricTraceLogWriteException(String info, Throwable e) {
        super(EnclaveNativeInvokingException.ENCLAVE_REMOTE_ATTESTATION_ERROR.buildExceptionMessage(info), e);
    }
}
