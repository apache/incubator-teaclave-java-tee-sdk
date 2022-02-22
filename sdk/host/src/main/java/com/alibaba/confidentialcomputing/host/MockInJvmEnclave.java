package com.alibaba.confidentialcomputing.host;

import com.alibaba.confidentialcomputing.host.exception.RemoteAttestationException;

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
    AttestationReport generateAttestationReport(byte[] userData) throws RemoteAttestationException {
        throw new RemoteAttestationException("MOCK_IN_JVM enclave doesn't support remote attestation generation.");
    }

    static int verifyAttestationReport(byte[] report) throws RemoteAttestationException {
        throw new RemoteAttestationException("MOCK_IN_JVM enclave doesn't support remote attestation verification.");
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
