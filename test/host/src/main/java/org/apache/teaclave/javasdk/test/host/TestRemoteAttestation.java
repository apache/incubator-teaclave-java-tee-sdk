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

package org.apache.teaclave.javasdk.test.host;

import org.apache.teaclave.javasdk.host.*;
import org.apache.teaclave.javasdk.host.exception.EnclaveCreatingException;
import org.apache.teaclave.javasdk.host.exception.EnclaveDestroyingException;
import org.apache.teaclave.javasdk.host.exception.RemoteAttestationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class TestRemoteAttestation {

    private void remoteAttestation(EnclaveType type) throws EnclaveCreatingException, RemoteAttestationException, EnclaveDestroyingException {
        try {
            Enclave enclave = EnclaveFactory.create(type);
            assertNotNull(enclave);
            byte[] userData = new byte[64];
            new Random().nextBytes(userData);

            SGXAttestationReport report = (SGXAttestationReport) RemoteAttestation.generateAttestationReport(enclave, userData);
            assertEquals(report.getEnclaveType(), type);
            assertNotNull(report.getQuote());
            assertEquals(0, RemoteAttestation.verifyAttestationReport(report));
            assertNotNull(report.getMeasurementEnclave());
            assertNotNull(report.getMeasurementSigner());
            assertNotNull(report.getUserData());
            assertArrayEquals(userData, report.getUserData());
            enclave.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Before
    public final void before() { System.out.println("enter test case: " + this.getClass().getName()); }

    @After
    public final void after() { System.out.println("exit test case: " + this.getClass().getName()); }

    @Test
    public void testRemoteAttestation() throws Exception {
        remoteAttestation(EnclaveType.TEE_SDK);
        remoteAttestation(EnclaveType.EMBEDDED_LIB_OS);
    }
}
