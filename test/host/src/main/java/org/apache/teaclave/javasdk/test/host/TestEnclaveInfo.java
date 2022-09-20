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

import org.apache.teaclave.javasdk.host.Enclave;
import org.apache.teaclave.javasdk.host.EnclaveFactory;
import org.apache.teaclave.javasdk.host.EnclaveInfo;
import org.apache.teaclave.javasdk.host.EnclaveType;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestEnclaveInfo {

    @Test
    public void testEnclaveInfo() throws Exception {
        Enclave enclave = EnclaveFactory.create(EnclaveType.MOCK_IN_JVM);
        EnclaveInfo enclaveInfo = enclave.getEnclaveInfo();
        assertEquals(enclaveInfo.getEnclaveType(), EnclaveType.MOCK_IN_JVM);
        assertTrue(enclaveInfo.isEnclaveDebuggable());
        assertEquals(enclaveInfo.getEnclaveEPCMemorySizeBytes(), -1);
        assertEquals(enclaveInfo.getEnclaveMaxThreadsNumber(), -1);
        enclave.destroy();

        enclave = EnclaveFactory.create(EnclaveType.MOCK_IN_SVM);
        enclaveInfo = enclave.getEnclaveInfo();
        assertEquals(enclaveInfo.getEnclaveType(), EnclaveType.MOCK_IN_SVM);
        assertTrue(enclaveInfo.isEnclaveDebuggable());
        assertEquals(enclaveInfo.getEnclaveEPCMemorySizeBytes(), -1);
        assertEquals(enclaveInfo.getEnclaveMaxThreadsNumber(), -1);
        enclave.destroy();

        // it's related to config file in test project.
        enclave = EnclaveFactory.create(EnclaveType.TEE_SDK);
        enclaveInfo = enclave.getEnclaveInfo();
        assertEquals(enclaveInfo.getEnclaveType(), EnclaveType.TEE_SDK);
        assertFalse(enclaveInfo.isEnclaveDebuggable());
        assertEquals(enclaveInfo.getEnclaveEPCMemorySizeBytes(), 1500 * 1024 * 1024);
        assertEquals(enclaveInfo.getEnclaveMaxThreadsNumber(), 50);
        enclave.destroy();

        // it's related to config file in test project.
        enclave = EnclaveFactory.create(EnclaveType.EMBEDDED_LIB_OS);
        enclaveInfo = enclave.getEnclaveInfo();
        assertEquals(enclaveInfo.getEnclaveType(), EnclaveType.EMBEDDED_LIB_OS);
        assertFalse(enclaveInfo.isEnclaveDebuggable());
        assertEquals(enclaveInfo.getEnclaveEPCMemorySizeBytes(), 1500 * 1024 * 1024);
        assertEquals(enclaveInfo.getEnclaveMaxThreadsNumber(), 50);
        enclave.destroy();
    }
}
