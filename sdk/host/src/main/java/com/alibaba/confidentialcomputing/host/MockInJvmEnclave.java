package com.alibaba.confidentialcomputing.host;

/**
 * MockInJvmEnclave is a mock jvm enclave. Both host and enclave codes run
 * in one jvm. It was used for test and debug.
 */
class MockInJvmEnclave extends AbstractEnclave {
    MockInJvmEnclave() {
        // Set EnclaveContext for this enclave instance.
        super(EnclaveType.MOCK_IN_JVM, new BaseEnclaveServicesRecycler());
    }

    @Override
    InnerNativeInvocationResult loadServiceNative(byte[] payload) {
        return null;
    }

    @Override
    InnerNativeInvocationResult unloadServiceNative(byte[] payload) {
        return null;
    }

    @Override
    InnerNativeInvocationResult invokeMethodNative(byte[] payload) {
        return null;
    }

    @Override
    public void destroy() {
        ; // Do nothing here.
    }
}
