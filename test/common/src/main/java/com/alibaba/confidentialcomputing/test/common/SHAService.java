package com.alibaba.confidentialcomputing.test.common;

import com.alibaba.confidentialcomputing.common.annotations.EnclaveService;

@EnclaveService
public interface SHAService {
    String encryptPlaintext(String plaintext, String SHAType) throws Exception;
}
