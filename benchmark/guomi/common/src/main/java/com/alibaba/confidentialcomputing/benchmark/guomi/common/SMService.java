package com.alibaba.confidentialcomputing.benchmark.guomi.common;

import com.alibaba.confidentialcomputing.common.annotations.EnclaveService;

@EnclaveService
public interface SMService {
    String sm2Service(String plaintext, int weight) throws Exception;
    byte[] sm3Service(String plainText, int weight) throws Exception;
    String sm4Service(String plaintext, int weight) throws Exception;
}
