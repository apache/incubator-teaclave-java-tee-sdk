package com.alibaba.confidentialcomputing.test.common;

import com.alibaba.confidentialcomputing.common.annotations.EnclaveService;

@EnclaveService
public interface SM4Service {
    String sm4Service(String plaintext) throws Exception;
}
