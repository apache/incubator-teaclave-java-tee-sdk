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

import org.apache.teaclave.javasdk.test.common.SM4Service;
import com.google.auto.service.AutoService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;

@AutoService(SM4Service.class)
public class SM4ServiceImpl implements SM4Service {
    private static final String ALGORITHM_NAME = "SM4";
    private static final String ALGORITHM_ECB_PKCS5PADDING = "SM4/ECB/PKCS5Padding";
    private static final int DEFAULT_KEY_SIZE = 128;

    static {
        Provider provider = Security.getProvider("BC");
        if (provider == null) {
            provider = new BouncyCastleProvider();
        }
        Security.addProvider(provider);
    }

    private byte[] generateKey() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM_NAME, BouncyCastleProvider.PROVIDER_NAME);
        kg.init(DEFAULT_KEY_SIZE, new SecureRandom());
        return kg.generateKey().getEncoded();
    }

    private byte[] sm4EncryptAndDecrypt(byte[] data, byte[] key, int mode) throws Exception {
        SecretKeySpec sm4Key = new SecretKeySpec(key, ALGORITHM_NAME);
        Cipher cipher = Cipher.getInstance(SM4ServiceImpl.ALGORITHM_ECB_PKCS5PADDING, BouncyCastleProvider.PROVIDER_NAME);
        cipher.init(mode, sm4Key);
        return cipher.doFinal(data);
    }

    @Override
    public String sm4Service(String plaintext) throws Exception {
        byte[] key = generateKey();
        byte[] encryptResult = sm4EncryptAndDecrypt(plaintext.getBytes(StandardCharsets.UTF_8), key, Cipher.ENCRYPT_MODE);
        return new String(sm4EncryptAndDecrypt(encryptResult, key, Cipher.DECRYPT_MODE), StandardCharsets.UTF_8);
    }
}
