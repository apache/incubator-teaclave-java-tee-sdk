package com.alibaba.enclave.bouncycatsle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class BouncyCastleTest {
    @Test
    public void testBC() {
        BcService service = new BcServiceImpl();
        String plainText = "Hello World";
        service.sm2Service(plainText);
        service.sm3Service(plainText);
        service.digestService("MD5", plainText);
        service.digestService("SHA-256", plainText);
        service.digestService("SHA-512", plainText);
        assertEquals(service.rsaService(plainText), plainText);
    }

    @Test
    public void testBCProvider() {
        String plainText = "Hello World";
        Sm2Service sm2ServiceImpl = new Sm2ServiceImpl();
        String publicKey = sm2ServiceImpl.getPublicKey();
        String ret = sm2ServiceImpl.encode(plainText, publicKey);
        assertNotNull(ret);
    }
}
