package com.alibaba.confidentialcomputing.test.enclave;

import com.alibaba.confidentialcomputing.test.common.AESSealedTest;
import com.alibaba.confidentialcomputing.test.common.AESService;
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
