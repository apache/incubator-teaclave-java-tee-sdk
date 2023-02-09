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
import org.apache.teaclave.javasdk.host.MetricTrace;
import org.apache.teaclave.javasdk.test.common.MetricTraceService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class TestEnclaveMetricTrace {
    private String invertCharacter(String str) {
        byte[] content = new byte[str.length()];
        byte[] initial = str.getBytes();
        for (int i = 0x0; i < initial.length; i++) {
            content[i] = initial[initial.length - i - 1];
        }
        return new String(content);
    }

    @Before
    public final void before() { System.out.println("enter test case: " + this.getClass().getName()); }

    @After
    public final void after() { System.out.println("exit test case: " + this.getClass().getName()); }

    @Test
    public void testEnclaveMetricTrace() throws Exception {
        MetricTrace.setEnclaveMetricTraceSwitch(true);
        String plaintext = "ABC_DEF_GHI_JKL_MNO_PQR_STU_VWX_YZ";
        EnclaveType[] types = new EnclaveType[] {
                EnclaveType.MOCK_IN_JVM,
                EnclaveType.MOCK_IN_SVM,
                EnclaveType.TEE_SDK};
        for (EnclaveType type : types) {
            Enclave enclave = EnclaveFactory.create(type);
            assertNotNull(enclave);
            Iterator<MetricTraceService> userServices = enclave.load(MetricTraceService.class);
            assertNotNull(userServices);
            assertTrue(userServices.hasNext());
            MetricTraceService service = userServices.next();
            String result = service.invertCharacter(plaintext);
            assertEquals(result, invertCharacter(plaintext));
            enclave.destroy();
        }
        MetricTrace.setEnclaveMetricTraceSwitch(false);

        Field flog = MetricTrace.class.getDeclaredField("logPath");
        flog.setAccessible(true);
        String logPath = (String) flog.get(null);
        assertNotNull(logPath);
        File file = new File(logPath);
        assertTrue(file.exists());
        InputStream in = new FileInputStream(logPath);
        byte[] buffer = new byte[in.available()];
        in.read(buffer);
        String str = new String(buffer);
        assertTrue(str.contains("enclave_creating_cost"));
        assertTrue(str.contains("enclave_destroying_cost"));
        assertTrue(str.contains("enclave_service_loading"));
        assertTrue(str.contains("TEE_SDK"));
        // assertTrue(str.contains("EMBEDDED_LIB_OS"));
        assertTrue(file.delete());
    }
}
