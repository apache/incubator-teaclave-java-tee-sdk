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

package org.apache.teaclave.javasdk.samples.springboot.enclave;

import org.apache.teaclave.javasdk.samples.springboot.common.SBEnclaveService;
import com.google.auto.service.AutoService;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.Objects;

@AutoService(SBEnclaveService.class)
public class EnclaveServiceImpl implements SBEnclaveService {
    private BouncyCastleProvider provider;
    private ECParameterSpec ecParameterSpec;
    private KeyFactory keyFactory;
    private String publicKey;
    private String privateKey;

    public EnclaveServiceImpl() {
        try {
            provider = (BouncyCastleProvider) Security.getProvider("BC");
            if (provider == null) {
                provider = new BouncyCastleProvider();
            }
            X9ECParameters parameters = GMNamedCurves.getByName("sm2p256v1");
            ecParameterSpec = new ECParameterSpec(parameters.getCurve(), parameters.getG(), parameters.getN(), parameters.getH());
            keyFactory = KeyFactory.getInstance("EC", provider);
            generateSm2KeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateSm2KeyPair() throws Exception {
        final ECGenParameterSpec sm2Spec = new ECGenParameterSpec("sm2p256v1");
        final KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", provider);
        SecureRandom random = new SecureRandom();
        kpg.initialize(sm2Spec, random);
        KeyPair keyPair = kpg.generateKeyPair();
        BCECPrivateKey privateKey = (BCECPrivateKey) keyPair.getPrivate();
        BCECPublicKey publicKey = (BCECPublicKey) keyPair.getPublic();
        this.publicKey = new String(Hex.encode(publicKey.getQ().getEncoded(true)));
        this.privateKey = privateKey.getD().toString(16);
    }

    private String encode(String input, String pubKey) {
        try {
            X9ECParameters parameters = GMNamedCurves.getByName("sm2p256v1");
            ECParameterSpec ecParameterSpec = new ECParameterSpec(parameters.getCurve(), parameters.getG(), parameters.getN(), parameters.getH());
            ECPoint ecPoint = parameters.getCurve().decodePoint(Hex.decode(pubKey));
            KeyFactory keyFactory = KeyFactory.getInstance("EC", provider);
            BCECPublicKey key = (BCECPublicKey) keyFactory.generatePublic(new ECPublicKeySpec(ecPoint, ecParameterSpec));
            Cipher cipher = Cipher.getInstance("SM2", provider);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(input.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] decoder(String input, String prvKey) {
        try {
            Cipher cipher = Cipher.getInstance("SM2", provider);
            BigInteger bigInteger = new BigInteger(prvKey, 16);
            BCECPrivateKey privateKey = (BCECPrivateKey) keyFactory.generatePrivate(new ECPrivateKeySpec(bigInteger, ecParameterSpec));
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(Base64.getDecoder().decode(input));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String digestData(String data) {
        byte[] messages = data.getBytes();
        Digest md = new SM3Digest();
        md.update(messages, 0, messages.length);
        byte[] digest = new byte[md.getDigestSize()];
        md.doFinal(digest, 0);
        return new String(digest);
    }

    @Override
    public String encryptAndDecryptData(String data) {
        return new String(Objects.requireNonNull(decoder(encode(data, publicKey), privateKey)));
    }
}
