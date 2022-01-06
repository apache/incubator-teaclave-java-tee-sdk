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

    /**
     * @param info exception message.
     * @param e    exception.
     */
    public ConfidentialComputingException(String info, Throwable e) {
        super(info, e);
    }

    /**
     * EnclaveNativeInvokingException defines all kinds of possible exceptions towards an
     * enclave's native operation. Basically there are two kinds error about enclave operation,
     * one kind is native calling return an unexpected value, the other kind is an exception
     * happen in enclave and transform into host side. If a native invoking into enclave returns
     * an error value, enum of EnclaveNativeInvokingException will add extra error message details
     * for debugging; If an exception happened in enclave and transformed to host side, it will be
     * thrown again in host side to user.
     * Programmers need to handle EnclaveNativeInvokingException seriously.
     */
    enum EnclaveNativeInvokingException {
        // Enclave creating failed.
        ENCLAVE_CREATING_ERROR("A0001", "creating enclave failed."),
        // Enclave destroying failed.
        ENCLAVE_DESTROYING_ERROR("A0002", "destroying enclave failed"),
        // Services loading failed.
        SERVICES_LOADING_ERROR("A0003", "services loading failed in enclave"),
        // Services unloading failed.
        SERVICES_UNLOADING_ERROR("A0004", "service unloading failed in enclave"),
        // Service method invoking failed.
        SERVICE_METHOD_INVOKING_ERROR("A0005", "service method invoking failed");

        private final String errorCode;
        private final String errorMessage;

        EnclaveNativeInvokingException(String errorCode, String errorMessage) {
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        String buildExceptionMessage(String details) {
            if (details != null) {
                return this.toString() + " DetailErrorMessage: " + details;
            } else {
                return this.toString();
            }
        }

        @Override
        public String toString() {
            return "ErrorCode: " + this.errorCode + " , " + " ErrorMessage: " + this.errorMessage;
        }
    }
}