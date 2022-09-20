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
import org.apache.teaclave.javasdk.test.common.SM2Service;
import org.apache.teaclave.javasdk.test.common.SM3Service;
import org.apache.teaclave.javasdk.test.common.SM4Service;
import org.apache.teaclave.javasdk.test.common.SMSignAndVerify;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class TestSMEnclave {
    private byte[] sm3Digest(String plaintext) {
        byte[] messages = plaintext.getBytes();
        Digest md = new SM3Digest();
        md.update(messages, 0, messages.length);
        byte[] digest = new byte[md.getDigestSize()];
        md.doFinal(digest, 0);
        return digest;
    }

    @Test
    public void testSM2Enclave() throws Exception {
        String plaintext = "Hello World!!!";
        EnclaveType[] types = new EnclaveType[]{
                EnclaveType.MOCK_IN_JVM,
                EnclaveType.MOCK_IN_SVM,
                EnclaveType.TEE_SDK,
                EnclaveType.EMBEDDED_LIB_OS};

        for (EnclaveType type : types) {
            Enclave enclave = EnclaveFactory.create(type);
            assertNotNull(enclave);
            Iterator<SM2Service> userServices = enclave.load(SM2Service.class);
            assertNotNull(userServices);
            assertTrue(userServices.hasNext());
            SM2Service service = userServices.next();
            String result = service.encryptAndDecryptWithPlaintext(plaintext);
            assertEquals(plaintext, result);
            enclave.destroy();
        }
    }

    @Test
    public void testSM3Enclave() throws Exception {
        String plaintext = "Hello World!!!";
        EnclaveType[] types = new EnclaveType[]{
                EnclaveType.MOCK_IN_JVM,
                EnclaveType.MOCK_IN_SVM,
                EnclaveType.TEE_SDK,
                EnclaveType.EMBEDDED_LIB_OS};

        for (EnclaveType type : types) {
            Enclave enclave = EnclaveFactory.create(type);
            assertNotNull(enclave);
            Iterator<SM3Service> userServices = enclave.load(SM3Service.class);
            assertNotNull(userServices);
            assertTrue(userServices.hasNext());
            SM3Service service = userServices.next();
            byte[] result = service.sm3Service(plaintext);
            assertArrayEquals(sm3Digest(plaintext), result);
            enclave.destroy();
        }
    }

    @Test
    public void testSM4Enclave() throws Exception {
        String plaintext = "Hello World!!!";
        EnclaveType[] types = new EnclaveType[]{
                EnclaveType.MOCK_IN_JVM,
                EnclaveType.MOCK_IN_SVM,
                EnclaveType.TEE_SDK,
                EnclaveType.EMBEDDED_LIB_OS};

        for (EnclaveType type : types) {
            Enclave enclave = EnclaveFactory.create(type);
            assertNotNull(enclave);
            Iterator<SM4Service> userServices = enclave.load(SM4Service.class);
            assertNotNull(userServices);
            assertTrue(userServices.hasNext());
            SM4Service service = userServices.next();
            assertEquals(service.sm4Service(plaintext), plaintext);
            enclave.destroy();
        }
    }

    @Test
    public void testSMSignAndVerify() throws Exception {
        String plaintext = "Hello World!!!";
        EnclaveType[] types = new EnclaveType[]{
                EnclaveType.MOCK_IN_JVM,
                EnclaveType.MOCK_IN_SVM,
                EnclaveType.TEE_SDK,
                EnclaveType.EMBEDDED_LIB_OS};

        for (EnclaveType type : types) {
            Enclave enclave = EnclaveFactory.create(type);
            assertNotNull(enclave);
            Iterator<SMSignAndVerify> userServices = enclave.load(SMSignAndVerify.class);
            assertNotNull(userServices);
            assertTrue(userServices.hasNext());
            SMSignAndVerify service = userServices.next();
            assertTrue(service.smSignAndVerify(plaintext));
            enclave.destroy();
        }
    }
}
