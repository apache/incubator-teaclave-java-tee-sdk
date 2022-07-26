package com.alibaba.enclave.bouncycatsle;

public interface Sm2Service {
    String getPublicKey();
    String getPrivateKey();
    String encode(String input, String pubKey);
    byte[] decoder(String input, String prvKey);
    String sign(String plainText, String prvKey);
    boolean verify(String plainText, String signatureValue, String pubKey);
    boolean certVerify(String certStr, String plaintext, String signValueStr);
}