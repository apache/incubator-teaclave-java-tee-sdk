package com.alibaba.confidentialcomputing.test.common;

import com.alibaba.confidentialcomputing.common.annotations.EnclaveService;

@EnclaveService
public interface AESService {
    String aesEncryptAndDecryptPlaintext(String plaintext) throws Exception;
    String aesEncryptAndDecryptPlaintextWithPassword(String plaintext, String password, String salt) throws Exception;
    Object aesEncryptAndDecryptObject(AESSealedTest obj) throws Exception;
}
