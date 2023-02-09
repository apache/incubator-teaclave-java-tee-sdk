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
import org.apache.teaclave.javasdk.host.EnclaveType;
import org.apache.teaclave.javasdk.test.common.EnclaveServiceStatistic;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class TestEnclaveServiceGC {
    private void enclaveServiceGC(EnclaveType type) throws Exception {
        int count = 1001;
        Enclave enclave = EnclaveFactory.create(type);
        assertNotNull(enclave);
        for (int i = 0x0; i < count; i++) {
            Iterator<EnclaveServiceStatistic> userServices = enclave.load(EnclaveServiceStatistic.class);
            assertNotNull(userServices);
            assertTrue(userServices.hasNext());
        }
        System.gc();
        Thread.sleep(1000);
        System.gc();
        Thread.sleep(1000);
        Iterator<EnclaveServiceStatistic> userServices = enclave.load(EnclaveServiceStatistic.class);
        assertEquals(1, userServices.next().getEnclaveServiceCount());
        enclave.destroy();
    }

    @Before
    public final void before() { System.out.println("enter test case: " + this.getClass().getName()); }

    @After
    public final void after() { System.out.println("exit test case: " + this.getClass().getName()); }

    @Test
    public void testEnclaveServiceGC() throws Exception {
        enclaveServiceGC(EnclaveType.MOCK_IN_SVM);
        enclaveServiceGC(EnclaveType.TEE_SDK);
        // enclaveServiceGC(EnclaveType.EMBEDDED_LIB_OS);
    }
}
