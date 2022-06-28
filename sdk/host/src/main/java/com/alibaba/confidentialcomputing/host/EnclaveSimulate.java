package com.alibaba.confidentialcomputing.host;

/**
 * An enumeration of enclave simulate mode.
 */
enum EnclaveSimulate {
    /**
     * For MOCK_IN_JVM and MOCK_IN_SVM, there is no real enclave environment.
     */
    NONE(0),
    /**
     * TEE_SDK/EMBEDDED_LIB_OS could run in simulate mode without sgx.
     */
    SIMULATE(1),
    /**
     * TEE_SDK/EMBEDDED_LIB_OS could run in hardware mode with sgx.
     */
    HARDWARE(2);

    private final int value;

    EnclaveSimulate(int value) {
        this.value = value;
    }

    int getValue() {
        return value;
    }
}
