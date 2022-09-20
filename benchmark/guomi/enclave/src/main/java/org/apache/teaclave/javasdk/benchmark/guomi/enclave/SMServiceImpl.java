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

package org.apache.teaclave.javasdk.benchmark.guomi.enclave;

import org.apache.teaclave.javasdk.benchmark.guomi.common.SMService;
import com.google.auto.service.AutoService;

@AutoService(SMService.class)
public class SMServiceImpl implements SMService {

    @Override
    public String sm2Service(String plaintext, int weight) throws Exception {
        String result = null;
        for (int i = 0x0; i < weight; i++) {
            result = new SM2ServiceImpl().sm2Service(plaintext);
        }
        return result;
    }

    @Override
    public byte[] sm3Service(String plainText, int weight) throws Exception {
        byte[] result = null;
        for (int i = 0x0; i < weight; i++) {
            result = new SM3ServiceImpl().sm3Service(plainText);
        }
        return result;
    }

    @Override
    public String sm4Service(String plaintext, int weight) throws Exception {
        String result = null;
        for (int i = 0x0; i < weight; i++) {
            result = new SM4ServiceImpl().sm4Service(plaintext);
        }
        return result;
    }
}
