package com.alibaba.confidentialcomputing.host;

import com.alibaba.confidentialcomputing.common.EnclaveInvocationContext;
import com.alibaba.confidentialcomputing.common.ServiceHandler;
import com.alibaba.confidentialcomputing.host.exception.*;

import java.io.IOException;

/**
 * MockInJvmEnclave is a mock jvm enclave. Both host and enclave codes run
 * in one jvm. It was used for test and debug.
 */
class MockInJvmEnclave extends AbstractEnclave {
    private final MockEnclaveInfo enclaveInfo;

    MockInJvmEnclave() throws IOException {
        // Set EnclaveContext for this enclave instance.
        super(EnclaveType.MOCK_IN_JVM, new BaseEnclaveServicesRecycler());
        enclaveInfo = new MockEnclaveInfo(EnclaveType.MOCK_IN_JVM, true, -1, -1);
    }

    @Override
    AttestationReport generateAttestationReportNative(byte[] userData) throws RemoteAttestationException {
        throw new RemoteAttestationException("MOCK_IN_JVM enclave doesn't support remote attestation generation.");
    }

    static int verifyAttestationReport(byte[] report) throws RemoteAttestationException {
        throw new RemoteAttestationException("MOCK_IN_JVM enclave doesn't support remote attestation verification.");
    }

    @Override
    byte[] loadServiceNative(String service) throws ServicesLoadingException {
        return null;
    }

    @Override
    byte[] unloadServiceNative(ServiceHandler handler) throws ServicesUnloadingException {
        return null;
    }

    @Override
    byte[] invokeMethodNative(EnclaveInvocationContext context) throws EnclaveMethodInvokingException {
        return null;
    }

    @Override
    public EnclaveInfo getEnclaveInfo() {
        return enclaveInfo;
    }

    @Override
    public void destroy() throws EnclaveDestroyingException {
        try (MetricTraceContext trace = new MetricTraceContext(
                this.getEnclaveInfo(),
                MetricTraceContext.LogPrefix.METRIC_LOG_ENCLAVE_DESTROYING_PATTERN)) {
            EnclaveInfoManager.getEnclaveInfoManagerInstance().removeEnclave(this);
        } catch (MetricTraceLogWriteException e) {
            throw new EnclaveDestroyingException(e);
        }
    }
}
