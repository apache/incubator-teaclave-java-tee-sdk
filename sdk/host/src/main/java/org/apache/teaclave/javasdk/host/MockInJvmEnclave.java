// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.teaclave.javasdk.host;

import org.apache.teaclave.javasdk.common.EnclaveInvocationContext;
import org.apache.teaclave.javasdk.common.ServiceHandler;
import org.apache.teaclave.javasdk.host.exception.*;

import java.io.IOException;

/**
 * MockInJvmEnclave is a mock jvm enclave. Both host and enclave codes run
 * in one jvm. It was used for test and debug.
 */
final class MockInJvmEnclave extends AbstractEnclave {
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

    static int verifyAttestationReport(byte[] ignoredReport) throws RemoteAttestationException {
        throw new RemoteAttestationException("MOCK_IN_JVM enclave doesn't support remote attestation verification.");
    }

    @Override
    byte[] loadServiceNative(String service) {
        return null;
    }

    @Override
    byte[] unloadServiceNative(ServiceHandler handler) {
        return null;
    }

    @Override
    byte[] invokeMethodNative(EnclaveInvocationContext context) {
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
