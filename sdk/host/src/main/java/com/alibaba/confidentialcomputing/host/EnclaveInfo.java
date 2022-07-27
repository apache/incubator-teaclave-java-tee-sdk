package com.alibaba.confidentialcomputing.host;

/**
 * an enclave's detail information.
 */
public interface EnclaveInfo {
    /**
     * the enclave's type, MOCK_IN_JVM、MOCK_IN_SVM、TEE_SDK or EMBEDDED_LIB_OS.
     */
    EnclaveType getEnclaveType();

    /**
     * is the enclave debuggable or not. MOCK_IN_JVM and MOCK_IN_SVM are simulation mode,
     * so the two mock enclave type are debuggable. TEE_SDK and EMBEDDED_LIB_OS depend on
     * user, if the enclave is not debuggable, we couldn't debug the code run in enclave by
     * gdb or other debug tools.
     */
    boolean isEnclaveDebuggable();

    /**
     * get enclave's usable epc memory size.
     */
    long getEnclaveEPCMemorySizeBytes();

    /**
     * get enclave's usable max threads number.
     */
    int getEnclaveMaxThreadsNumber();
}
