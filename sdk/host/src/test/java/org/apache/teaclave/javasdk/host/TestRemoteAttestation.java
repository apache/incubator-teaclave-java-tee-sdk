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

import org.apache.teaclave.javasdk.host.exception.RemoteAttestationException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class TestRemoteAttestation {
    @Test
    void testRemoteAttestation() throws IOException {
        Enclave mockInJvmEnclave = new MockInJvmEnclave();
        assertThrows(RemoteAttestationException.class, () -> RemoteAttestation.generateAttestationReport(mockInJvmEnclave, null));
        assertThrows(RemoteAttestationException.class, () -> RemoteAttestation.verifyAttestationReport(new AttestationReport(EnclaveType.MOCK_IN_JVM, null)));
        assertThrows(RemoteAttestationException.class, () -> RemoteAttestation.verifyAttestationReport(new AttestationReport(EnclaveType.MOCK_IN_SVM, null)));
    }

    @Test
    void testNormalizeUserData() throws Exception {
        Class<RemoteAttestation> clazz = RemoteAttestation.class;
        Method method = clazz.getDeclaredMethod("normalizeUserData", byte[].class);
        method.setAccessible(true);

        byte[] parameter = null;
        Object result = method.invoke(null, parameter);
        assertEquals(((byte[]) result).length, 64);

        parameter = new byte[32];
        result = method.invoke(null, parameter);
        assertEquals(((byte[]) result).length, 64);

        byte[] finalParameter = new byte[65];
        assertThrows(InvocationTargetException.class, () -> method.invoke(null, finalParameter));
    }

    @Test
    void testAttestationReport() {
        byte[] quote = new byte[4];
        Arrays.fill(quote, (byte) 0x5f);
        AttestationReport report = new AttestationReport(EnclaveType.TEE_SDK, quote);
        byte[] serializedReport = report.toByteArray();
        AttestationReport deserializedReport = AttestationReport.fromByteArray(serializedReport);
        assertEquals(EnclaveType.TEE_SDK, deserializedReport.getEnclaveType());
        for (int index = 0; index < quote.length; index++) {
            assertEquals(quote[index], (deserializedReport.getQuote())[index]);
        }

        report = new AttestationReport(EnclaveType.EMBEDDED_LIB_OS, quote);
        serializedReport = report.toByteArray();
        deserializedReport = AttestationReport.fromByteArray(serializedReport);
        assertEquals(EnclaveType.EMBEDDED_LIB_OS, deserializedReport.getEnclaveType());
        for (int index = 0; index < quote.length; index++) {
            assertEquals(quote[index], (deserializedReport.getQuote())[index]);
        }
    }
}
