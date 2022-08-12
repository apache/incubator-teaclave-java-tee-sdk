package com.alibaba.confidentialcomputing.benchmark.guomi.common;

import com.alibaba.confidentialcomputing.common.annotations.EnclaveService;

@EnclaveService
public interface SMService {
    String sm2Service(String plaintext) throws Exception;
    byte[] sm3Service(String plainText) throws Exception;
    String sm4Service(String plaintext) throws Exception;
}
