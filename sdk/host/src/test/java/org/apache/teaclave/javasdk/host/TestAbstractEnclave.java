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

import org.apache.teaclave.javasdk.common.ServiceHandler;
import org.apache.teaclave.javasdk.host.exception.EnclaveCreatingException;
import org.apache.teaclave.javasdk.host.exception.EnclaveMethodInvokingException;
import org.apache.teaclave.javasdk.host.exception.ServicesLoadingException;
import org.junit.jupiter.api.*;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Iterator;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class TestAbstractEnclave {
    private static Enclave enclave;

    @BeforeAll
    static void create() throws EnclaveCreatingException {
        enclave = new MockTestEnclave();
    }

    @Test
    void testEnclave() throws Exception {
        Iterator<?> services = enclave.load(Service.class);
        assertEquals(1, ((MockTestEnclave) enclave).getServicesNum());
        assertNotNull(services);
        assertTrue(services.hasNext());
        Service service = (Service) services.next();
        service.doNothing();
        assertEquals(200, service.add(20, 180));
        assertEquals("Hello World", service.saySomething("Hello World"));
        assertThrows(ServiceExceptionTest.class, () -> service.throwException("something is wrong"));
        Queue<?> queue = ((MockTestEnclave) enclave).getCachedServiceHandler();
        assertEquals(1, queue.size());
        ((MockTestEnclave) enclave).unloadService((ServiceHandler) queue.poll());
        assertEquals(0, ((MockTestEnclave) enclave).getServicesNum());
        try {
            service.doNothing();
        } catch (UndeclaredThrowableException e) {
            assertEquals(e.getCause().getClass(), EnclaveMethodInvokingException.class);
        }
        enclave.destroy();
        assertThrows(ServicesLoadingException.class, () -> enclave.load(Service.class));
    }
}
