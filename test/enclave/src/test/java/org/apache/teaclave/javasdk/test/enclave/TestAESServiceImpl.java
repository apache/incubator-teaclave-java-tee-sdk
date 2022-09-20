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

package org.apache.teaclave.javasdk.test.enclave;

import org.apache.teaclave.javasdk.test.common.AESSealedTest;
import org.apache.teaclave.javasdk.test.common.AESService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestAESServiceImpl {
    String plaintext = "Hello World!!!";

    @Test
    public void testAESServiceImpl() throws Exception {
        AESService service = new AESServiceImpl();
        String result = service.aesEncryptAndDecryptPlaintext(plaintext);
        assertEquals(plaintext, result);
        result = service.aesEncryptAndDecryptPlaintextWithPassword(plaintext, "javaenclave", "12345678");
        assertEquals(plaintext, result);
        AESSealedTest obj = new AESSealedTest("javaenclave", 25, 5);
        assertEquals(0, obj.compareTo((AESSealedTest) service.aesEncryptAndDecryptObject(obj)));
    }
}
