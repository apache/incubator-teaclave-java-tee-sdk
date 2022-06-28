package com.alibaba.confidentialcomputing.host;

/**
 * An enumeration of enclave debug mode.
 */
public enum EnclaveDebug {
    /**
     * For MOCK_IN_JVM and MOCK_IN_SVM, there is no real enclave environment.
     */
    NONE(0),
    /**
     * TEE_SDK could debug by gdb tool in this mode.
     */
    DEBUG(1),
    /**
     * TEE_SDK could not debug by gdb tool in this mode.
     */
    RELEASE(2);

    private final int value;

    EnclaveDebug(int value) {
        this.value = value;
    }

    int getValue() {
        return value;
    }
}
