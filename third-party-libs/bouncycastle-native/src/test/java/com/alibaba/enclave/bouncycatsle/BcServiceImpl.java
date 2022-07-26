package com.alibaba.enclave.bouncycatsle;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Provider;
import java.security.Security;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.util.Base64;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.jcajce.spec.SM2ParameterSpec;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class BcServiceImpl implements BcService {

    private final Provider PROVIDER;

    public BcServiceImpl() {
        Provider provider = Security.getProvider("BC");
        if (provider != null) {
            PROVIDER = provider;
        } else {
            PROVIDER = new BouncyCastleProvider();
        }
        Security.addProvider(PROVIDER);
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    @Override
    public String sm2Service(String plainText) {
        String result = "";
        try {
            ECParameterSpec sm2p256v1 = ECNamedCurveTable.getParameterSpec("sm2p256v1");
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC", PROVIDER);
            generator.initialize(sm2p256v1);
            KeyPair keyPair = generator.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            final byte[] data = plainText.getBytes();
            Signature signature = Signature.getInstance("SM3WithSM2", PROVIDER);
            // UserID
            SM2ParameterSpec spec = new SM2ParameterSpec("1234567812345678".getBytes());
            signature.initSign(privateKey);
            signature.setParameter(spec);
            signature.update(data);
            byte[] sign = signature.sign();
            signature.initVerify(publicKey);
            signature.update(data);
            if (signature.verify(sign)) {
                result = bytesToHex(sign);
            } else {
                result = "SM2 verify failed!!!";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String sm3Service(String plainText) {
        String result = "";
        try {
            byte[] messages = plainText.getBytes();
            Digest md = new SM3Digest();
            md.update(messages, 0, messages.length);
            byte[] digest = new byte[md.getDigestSize()];
            md.doFinal(digest, 0);
            result = bytesToHex(digest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String digestService(String type, String plainText) {
        String result = "";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(type);
            messageDigest.update(plainText.getBytes());
            byte[] byteBuffer = messageDigest.digest();
            result = bytesToHex(byteBuffer);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String rsaService(String plainText) {
        String result = "";
        try {
            RSAKeyPairGenerator rsaKeyPairGenerator = new RSAKeyPairGenerator();
            RSAKeyGenerationParameters rsaKeyGenerationParameters = new RSAKeyGenerationParameters(BigInteger.valueOf(3), new SecureRandom(), 1024, 25);
            rsaKeyPairGenerator.init(rsaKeyGenerationParameters);
            AsymmetricCipherKeyPair keyPair = rsaKeyPairGenerator.generateKeyPair();
            AsymmetricKeyParameter publicKey = keyPair.getPublic();
            AsymmetricKeyParameter privateKey = keyPair.getPrivate();

            SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(publicKey);
            PrivateKeyInfo privateKeyInfo = PrivateKeyInfoFactory.createPrivateKeyInfo(privateKey);
            ASN1Object asn1ObjectPublic = subjectPublicKeyInfo.toASN1Primitive();
            byte[] publicInfoByte = asn1ObjectPublic.getEncoded();
            ASN1Object asn1ObjectPrivate = privateKeyInfo.toASN1Primitive();
            byte[] privateInfoByte = asn1ObjectPrivate.getEncoded();
            final Base64.Encoder encoder64 = Base64.getEncoder();
            ASN1Object pubKeyObj = subjectPublicKeyInfo.toASN1Primitive();
            AsymmetricKeyParameter pubKey = PublicKeyFactory.createKey(SubjectPublicKeyInfo.getInstance(pubKeyObj));
            AsymmetricBlockCipher cipher = new RSAEngine();
            cipher.init(true, pubKey);
            final Base64.Decoder decoder64 = Base64.getDecoder();
            byte[] encryptData = cipher.processBlock(plainText.getBytes(StandardCharsets.UTF_8), 0, plainText.getBytes(StandardCharsets.UTF_8).length);
            AsymmetricKeyParameter priKey = PrivateKeyFactory.createKey(privateInfoByte);
            cipher.init(false, priKey);
            byte[] decriyptData = cipher.processBlock(encryptData, 0, encryptData.length);
            result = new String(decriyptData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
