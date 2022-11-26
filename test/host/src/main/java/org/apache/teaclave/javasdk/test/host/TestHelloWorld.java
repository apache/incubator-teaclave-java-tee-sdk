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
import org.apache.teaclave.javasdk.test.common.SayHelloService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class TestHelloWorld {

    private String sayHelloService(EnclaveType type, String plain) throws
            EnclaveCreatingException, ServicesLoadingException, EnclaveDestroyingException {
        Enclave enclave = EnclaveFactory.create(type);
        assertNotNull(enclave);
        Iterator<SayHelloService> userServices = enclave.load(SayHelloService.class);
        assertNotNull(userServices);
        assertTrue(userServices.hasNext());
        SayHelloService service = userServices.next();
        String result = service.sayHelloService(plain);
        assertEquals("Hello World", service.sayHelloWorld());
        enclave.destroy();
        return result;
    }

    @Before
    public final void before() { System.out.println("enter test case: " + this.getClass().getName()); }

    @After
    public final void after() { System.out.println("exit test case: " + this.getClass().getName()); }

    @Test
    public void testSayHelloService() throws Exception {
        assertEquals("Hello World", sayHelloService(EnclaveType.MOCK_IN_JVM, "Hello World"));
        assertEquals("Hello World", sayHelloService(EnclaveType.MOCK_IN_SVM, "Hello World"));
        assertEquals("Hello World", sayHelloService(EnclaveType.TEE_SDK, "Hello World"));
        assertEquals("Hello World", sayHelloService(EnclaveType.EMBEDDED_LIB_OS, "Hello World"));
    }
}
