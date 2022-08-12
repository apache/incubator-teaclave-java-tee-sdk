package com.alibaba.confidentialcomputing.samples.springboot.common;

import com.alibaba.confidentialcomputing.common.annotations.EnclaveService;

@EnclaveService
public interface SBEnclaveService {
    // calculate giving data's digest.
    String digestData(String data);
    // encrypt and decrypt giving string.
    String encryptAndDecryptData(String data);
}
