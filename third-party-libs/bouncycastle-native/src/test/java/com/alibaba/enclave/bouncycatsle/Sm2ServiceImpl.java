package com.alibaba.enclave.bouncycatsle;

import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.gm.GMObjectIdentifiers;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class Sm2ServiceImpl implements Sm2Service {

    private BouncyCastleProvider provider;
    private X9ECParameters parameters;
    private ECParameterSpec ecParameterSpec;
    private KeyFactory keyFactory;
    private String publicKey;
    private String privateKey;

    private void generateSm2KeyPair() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
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

    public Sm2ServiceImpl() {
        try {
            //provider = new BouncyCastleProvider();
            provider = (BouncyCastleProvider) Security.getProvider("BC");
            parameters = GMNamedCurves.getByName("sm2p256v1");
            ecParameterSpec = new ECParameterSpec(parameters.getCurve(), parameters.getG(), parameters.getN(), parameters.getH());
            keyFactory = KeyFactory.getInstance("EC", provider);
            generateSm2KeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getPublicKey() {
        return this.publicKey;
    }

    @Override
    public String getPrivateKey() {
        return this.privateKey;
    }

    @Override
    public String encode(String input, String pubKey) {
        try {
            X9ECParameters parameters = GMNamedCurves.getByName("sm2p256v1");
            ECParameterSpec ecParameterSpec = new ECParameterSpec(parameters.getCurve(), parameters.getG(), parameters.getN(), parameters.getH());
            ECPoint ecPoint = parameters.getCurve().decodePoint(Hex.decode(pubKey));
            KeyFactory keyFactory = KeyFactory.getInstance("EC", provider);
            BCECPublicKey key = (BCECPublicKey) keyFactory.generatePublic(new ECPublicKeySpec(ecPoint, ecParameterSpec));
            Cipher cipher = Cipher.getInstance("SM2", provider);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(input.getBytes()));
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | InvalidKeySpecException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public byte[] decoder(String input, String prvKey) {
        try {
            Cipher cipher = Cipher.getInstance("SM2", provider);
            BigInteger bigInteger = new BigInteger(prvKey, 16);
            BCECPrivateKey privateKey = (BCECPrivateKey) keyFactory.generatePrivate(new ECPrivateKeySpec(bigInteger, ecParameterSpec));
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(Base64.getDecoder().decode(input));
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | InvalidKeySpecException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String sign(String plainText, String prvKey) {
        try {
            Signature signature = Signature.getInstance(GMObjectIdentifiers.sm2sign_with_sm3.toString(), provider);
            BigInteger bigInteger = new BigInteger(prvKey, 16);
            BCECPrivateKey privateKey = (BCECPrivateKey) keyFactory.generatePrivate(new ECPrivateKeySpec(bigInteger, ecParameterSpec));
            signature.initSign(privateKey);
            signature.update(plainText.getBytes());
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean verify(String plainText, String signatureValue, String pubKey) {
        try {
            Signature signature = Signature.getInstance(GMObjectIdentifiers.sm2sign_with_sm3.toString(), provider);
            ECPoint ecPoint = parameters.getCurve().decodePoint(Hex.decode(pubKey));
            BCECPublicKey key = (BCECPublicKey) keyFactory.generatePublic(new ECPublicKeySpec(ecPoint, ecParameterSpec));
            signature.initVerify(key);
            signature.update(plainText.getBytes());
            return signature.verify(Base64.getDecoder().decode(signatureValue));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean certVerify(String certStr, String plaintext, String signValueStr) {
        try {
            byte[] signValue = Base64.getDecoder().decode(signValueStr);
            CertificateFactory factory = new CertificateFactory();
            X509Certificate certificate = (X509Certificate) factory.engineGenerateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(certStr)));
            Signature signature = Signature.getInstance(certificate.getSigAlgName(), provider);
            signature.initVerify(certificate);
            signature.update(plaintext.getBytes());
            return signature.verify(signValue);
        } catch (NoSuchAlgorithmException | CertificateException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
        return false;
    }
}