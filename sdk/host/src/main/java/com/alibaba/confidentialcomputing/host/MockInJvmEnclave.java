package com.alibaba.confidentialcomputing.host;

import com.alibaba.confidentialcomputing.host.exception.*;

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
    AttestationReport generateAttestationReportNative(byte[] userData) throws RemoteAttestationException {
        throw new RemoteAttestationException("MOCK_IN_JVM enclave doesn't support remote attestation generation.");
    }

    static int verifyAttestationReport(byte[] report) throws RemoteAttestationException {
        throw new RemoteAttestationException("MOCK_IN_JVM enclave doesn't support remote attestation verification.");
    }

    @Override
    byte[] loadServiceNative(byte[] payload) throws ServicesLoadingException {
        return null;
    }

    @Override
    byte[] unloadServiceNative(byte[] payload) throws ServicesUnloadingException {
        return null;
    }

    @Override
    byte[] invokeMethodNative(byte[] payload) throws EnclaveMethodInvokingException {
        return null;
    }

    @Override
    public void destroy() throws EnclaveDestroyingException {
        ; // Do nothing here.
    }
}
