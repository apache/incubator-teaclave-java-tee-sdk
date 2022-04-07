package com.alibaba.confidentialcomputing.enclave.testservice;

import com.alibaba.confidentialcomputing.common.annotations.EnclaveService;

import java.security.KeyPair;

@EnclaveService
public interface EncryptionService {
    KeyPair generateKeyPair();
}
