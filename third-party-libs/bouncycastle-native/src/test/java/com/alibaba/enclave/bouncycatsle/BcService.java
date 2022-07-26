package com.alibaba.enclave.bouncycatsle;

public interface BcService {
    String sm2Service(String plainText);

    String sm3Service(String plainText);

    String digestService(String type, String plainText);

    String rsaService(String plainText);
}
