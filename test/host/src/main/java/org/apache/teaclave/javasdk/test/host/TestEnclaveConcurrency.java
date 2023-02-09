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
import org.apache.teaclave.javasdk.test.common.ConcurrencyCalculate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class TestEnclaveConcurrency {
    private void enclaveConcurrency(EnclaveType type) throws Exception {
        int concurrency = 10;
        int workload = 10_000;
        CountDownLatch latch0 = new CountDownLatch(1);
        CountDownLatch latch1 = new CountDownLatch(concurrency);

        Enclave enclave = EnclaveFactory.create(type);
        Iterator<ConcurrencyCalculate> services = enclave.load(ConcurrencyCalculate.class);
        assertTrue(services.hasNext());
        ConcurrencyCalculate service = services.next();
        for (int i = 0; i < concurrency; i++) {
            new Thread(() -> {
                try {
                    latch0.await();
                    for (int i1 = 0; i1 < workload; i1++) {
                        service.add(1);
                    }
                    latch1.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
        latch0.countDown();
        latch1.await();
        assertEquals(concurrency * workload, service.sum());

        // waiting for enclave service recycle.
        System.gc();
        Thread.sleep(2000);
        enclave.destroy();
    }

    private void enclaveConcurrencySync(EnclaveType type) throws Exception {
        int concurrency = 10;
        int workload = 20_000;
        CountDownLatch latch0 = new CountDownLatch(1);
        CountDownLatch latch1 = new CountDownLatch(concurrency);

        Enclave enclave = EnclaveFactory.create(type);
        Iterator<ConcurrencyCalculate> services = enclave.load(ConcurrencyCalculate.class);
        assertTrue(services.hasNext());
        ConcurrencyCalculate service = services.next();
        for (int i = 0; i < concurrency; i++) {
            new Thread(() -> {
                try {
                    latch0.await();
                    service.addSync(workload);
                    latch1.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
        latch0.countDown();
        latch1.await();
        assertEquals(concurrency * workload, service.sumSync());

        // waiting for enclave service recycle.
        System.gc();
        Thread.sleep(2000);
        enclave.destroy();
    }

    @Before
    public final void before() { System.out.println("enter test case: " + this.getClass().getName()); }

    @After
    public final void after() { System.out.println("exit test case: " + this.getClass().getName()); }

    @Test
    public void testEnclaveConcurrency() throws Exception {
        enclaveConcurrency(EnclaveType.MOCK_IN_JVM);
        enclaveConcurrency(EnclaveType.MOCK_IN_SVM);
        enclaveConcurrency(EnclaveType.TEE_SDK);
    }

    @Test
    public void testEnclaveConcurrencySync() throws Exception {
        enclaveConcurrencySync(EnclaveType.MOCK_IN_JVM);
        enclaveConcurrencySync(EnclaveType.MOCK_IN_SVM);
        enclaveConcurrencySync(EnclaveType.TEE_SDK);
    }
}
