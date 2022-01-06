package com.alibaba.confidentialcomputing.host;

import com.alibaba.confidentialcomputing.host.exception.EnclaveCreatingException;

/**
 * EnclaveConfigure decides a new created enclave's type and debug mode.
 * If user creates an enclave with specific enclave type, that it is no
 * matter what system variable is. If user creates an enclave with no
 * specific enclave type, system variable is adapted to decide enclave's
 * type and debug mode. Default enclave type is TEE_SDK and debug mode
 * is RELEASE.
 */
class EnclaveConfigure {
    private static final String ENCLAVE_TYPE = "com.alibaba.enclave.type";
    private static final String ENCLAVE_DEBUG = "com.alibaba.enclave.debug";
    private static final EnclaveType enclaveType;
    private static final EnclaveDebug enclaveDebug;

    static {
        // Three kinds of enclave is supported, TEE_SDK/MOCK_IN_JVM/MOCK_IN_SVM
        String platform = System.getProperty(ENCLAVE_TYPE);
        String mode = System.getProperty(ENCLAVE_DEBUG);
        if (platform != null) {
            switch (platform) {
                case "TEE_SDK":
                    enclaveType = EnclaveType.TEE_SDK;
                    break;
                case "MOCK_IN_JVM":
                    enclaveType = EnclaveType.MOCK_IN_JVM;
                    break;
                case "MOCK_IN_SVM":
                    enclaveType = EnclaveType.MOCK_IN_SVM;
                    break;
                case "NONE":
                default:
                    enclaveType = EnclaveType.NONE;
            }
        } else {
            // Default enclave type is tee sdk.
            enclaveType = EnclaveType.TEE_SDK;
        }

        if (mode != null) {
            // Three kinds of enclave debug mode is supported, DEBUG/RELEASE
            // If TEE_SDK enclave is created as RELEASE mode, it can't be debugged
            // with GDB tool.
            switch (mode) {
                case "DEBUG":
                    enclaveDebug = EnclaveDebug.DEBUG;
                    break;
                case "RELEASE":
                    enclaveDebug = EnclaveDebug.RELEASE;
                    break;
                case "NONE":
                default:
                    enclaveDebug = EnclaveDebug.NONE;
            }
        } else {
            // Default debug mode is release.
            enclaveDebug = EnclaveDebug.RELEASE;
        }
    }

    // create an enclave without specific enclave type.
    // if -Dcom.alibaba.enclave.type is not set, TEE_SDK
    // type enclave will be created.
    static Enclave create() throws EnclaveCreatingException {
        return create(enclaveType);
    }

    // create an enclave with specific enclave type.
    static Enclave create(EnclaveType type) throws EnclaveCreatingException {
        switch (type) {
            case MOCK_IN_JVM:
                return new MockInJvmEnclave();
            case MOCK_IN_SVM:
                return new MockInSvmEnclave();
            case TEE_SDK:
                return new TeeSdkEnclave(enclaveDebug);
            case NONE:
            default:
                throw new EnclaveCreatingException("enclave type is not supported.");
        }
    }
}
