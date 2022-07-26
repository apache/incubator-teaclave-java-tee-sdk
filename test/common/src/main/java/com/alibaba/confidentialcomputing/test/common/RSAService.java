package com.alibaba.confidentialcomputing.test.common;

import com.alibaba.confidentialcomputing.common.annotations.EnclaveService;

@EnclaveService
public interface RSAService {
    String encryptAndDecryptWithPlaintext(String plaintext) throws Exception;
}
