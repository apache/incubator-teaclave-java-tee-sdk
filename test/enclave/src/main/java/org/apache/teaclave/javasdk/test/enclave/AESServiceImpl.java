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
import com.google.auto.service.AutoService;

import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

@AutoService(AESService.class)
public class AESServiceImpl implements AESService {
    @Override
    public String aesEncryptAndDecryptPlaintext(String plaintext) throws Exception {
        SecretKey key = AESUtil.generateKey(128);
        IvParameterSpec ivParameterSpec = AESUtil.generateIv();
        String algorithm = "AES/CBC/PKCS5Padding";

        String cipherText = AESUtil.encrypt(algorithm, plaintext, key, ivParameterSpec);
        return AESUtil.decrypt(algorithm, cipherText, key, ivParameterSpec);
    }

    @Override
    public String aesEncryptAndDecryptPlaintextWithPassword(String plaintext, String password, String salt) throws Exception {
        IvParameterSpec ivParameterSpec = AESUtil.generateIv();
        SecretKey key = AESUtil.getKeyFromPassword(password, salt);

        String cipherText = AESUtil.encryptPasswordBased(plaintext, key, ivParameterSpec);
        return AESUtil.decryptPasswordBased(cipherText, key, ivParameterSpec);
    }

    @Override
    public Object aesEncryptAndDecryptObject(AESSealedTest obj) throws Exception {
        SecretKey key = AESUtil.generateKey(128);
        IvParameterSpec ivParameterSpec = AESUtil.generateIv();
        String algorithm = "AES/CBC/PKCS5Padding";

        SealedObject sealedObject = AESUtil.encryptObject(algorithm, obj, key, ivParameterSpec);
        return AESUtil.decryptObject(algorithm, sealedObject, key, ivParameterSpec);
    }
}
