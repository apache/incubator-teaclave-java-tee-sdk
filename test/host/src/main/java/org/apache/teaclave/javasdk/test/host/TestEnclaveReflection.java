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
import org.apache.teaclave.javasdk.host.exception.EnclaveCreatingException;
import org.apache.teaclave.javasdk.host.exception.EnclaveDestroyingException;
import org.apache.teaclave.javasdk.host.exception.ServicesLoadingException;
import org.apache.teaclave.javasdk.test.common.ReflectionCallService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestEnclaveReflection {

    private void reflectionCallService(EnclaveType type) throws EnclaveCreatingException, ServicesLoadingException, EnclaveDestroyingException {
        Enclave enclave = EnclaveFactory.create(type);
        assertNotNull(enclave);
        Iterator<ReflectionCallService> userServices = enclave.load(ReflectionCallService.class);
        assertNotNull(userServices);
        assertTrue(userServices.hasNext());
        ReflectionCallService service = userServices.next();
        assertEquals(20, service.add(2, 18));
        assertEquals(-20, service.sub(2, 22));
        enclave.destroy();
    }

    @Before
    public final void before() { System.out.println("enter test case: " + this.getClass().getName()); }

    @After
    public final void after() { System.out.println("exit test case: " + this.getClass().getName()); }

    @Test
    public void testReflectionCallService() throws Exception {
        reflectionCallService(EnclaveType.MOCK_IN_JVM);
        reflectionCallService(EnclaveType.MOCK_IN_SVM);
        reflectionCallService(EnclaveType.TEE_SDK);
        reflectionCallService(EnclaveType.EMBEDDED_LIB_OS);
    }
}
