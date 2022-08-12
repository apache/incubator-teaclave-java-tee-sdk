package com.alibaba.confidentialcomputing.benchmark.guomi.enclave;

import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
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

class SM2ServiceImpl {
    private static BouncyCastleProvider provider;
    private static ECParameterSpec ecParameterSpec;
    private static KeyFactory keyFactory;
    private static String publicKey;
    private static String privateKey;

    static {
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

    private static void generateSm2KeyPair() throws Exception {
        final ECGenParameterSpec sm2Spec = new ECGenParameterSpec("sm2p256v1");
        final KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", provider);
        SecureRandom random = new SecureRandom();
        kpg.initialize(sm2Spec, random);
        KeyPair keyPair = kpg.generateKeyPair();
        BCECPrivateKey priKey = (BCECPrivateKey) keyPair.getPrivate();
        BCECPublicKey pubKey = (BCECPublicKey) keyPair.getPublic();
        publicKey = new String(Hex.encode(pubKey.getQ().getEncoded(true)));
        privateKey = priKey.getD().toString(16);
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

    String sm2Service(String plaintext) throws Exception {
        return new String(decoder(encode(plaintext, publicKey), privateKey));
    }
}
