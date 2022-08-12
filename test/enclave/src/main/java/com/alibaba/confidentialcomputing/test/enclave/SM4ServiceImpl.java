package com.alibaba.confidentialcomputing.test.enclave;

import com.alibaba.confidentialcomputing.test.common.SM4Service;
import com.google.auto.service.AutoService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
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

    private byte[] sm4EncryptAndDecrypt(byte[] data, byte[] key, String sm4mode, byte[] iv, int mode) throws Exception {
        IvParameterSpec ivParameterSpec = null;
        if (null != iv) {
            ivParameterSpec = new IvParameterSpec(iv);
        }
        SecretKeySpec sm4Key = new SecretKeySpec(key, ALGORITHM_NAME);
        Cipher cipher = Cipher.getInstance(sm4mode, BouncyCastleProvider.PROVIDER_NAME);
        if (null == ivParameterSpec) {
            cipher.init(mode, sm4Key);
        } else {
            cipher.init(mode, sm4Key, ivParameterSpec);
        }
        return cipher.doFinal(data);
    }

    @Override
    public String sm4Service(String plaintext) throws Exception {
        byte[] key = generateKey();
        byte[] encryptResult = sm4EncryptAndDecrypt(plaintext.getBytes(StandardCharsets.UTF_8), key, ALGORITHM_ECB_PKCS5PADDING, null, Cipher.ENCRYPT_MODE);
        return new String(sm4EncryptAndDecrypt(encryptResult, key, ALGORITHM_ECB_PKCS5PADDING, null, Cipher.DECRYPT_MODE), StandardCharsets.UTF_8);
    }
}
