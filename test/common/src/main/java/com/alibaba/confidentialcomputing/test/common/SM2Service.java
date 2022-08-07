package com.alibaba.confidentialcomputing.test.common;

import com.alibaba.confidentialcomputing.common.annotations.EnclaveService;

@EnclaveService
public interface SM2Service {
    String encryptAndDecryptWithPlaintext(String plaintext) throws Exception;
}
