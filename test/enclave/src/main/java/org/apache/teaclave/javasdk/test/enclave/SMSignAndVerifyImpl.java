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

import com.google.auto.service.AutoService;
import org.apache.teaclave.javasdk.test.common.SMSignAndVerify;
import org.bouncycastle.jcajce.spec.SM2ParameterSpec;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;

import java.security.*;

@AutoService(SMSignAndVerify.class)
public class SMSignAndVerifyImpl implements SMSignAndVerify {

    private final Provider PROVIDER;

    public SMSignAndVerifyImpl() {
        Provider provider = Security.getProvider("BC");
        if (provider != null) {
            PROVIDER = provider;
        } else {
            PROVIDER = new BouncyCastleProvider();
        }
        Security.addProvider(PROVIDER);
    }

    @Override
    public Boolean smSignAndVerify(String plaintext) throws Exception {
        Security.addProvider(PROVIDER);
        ECParameterSpec sm2p256v1 = ECNamedCurveTable.getParameterSpec("sm2p256v1");
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC", PROVIDER);
        generator.initialize(sm2p256v1);
        KeyPair keyPair = generator.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        Signature signature = Signature.getInstance("SM3WithSM2", PROVIDER);
        signature.initSign(privateKey);
        signature.setParameter(new SM2ParameterSpec("1234567812345678".getBytes()));
        signature.update(plaintext.getBytes());
        byte[] sign = signature.sign();

        signature.initVerify(publicKey);
        signature.update(plaintext.getBytes());
        return signature.verify(sign);
    }
}
