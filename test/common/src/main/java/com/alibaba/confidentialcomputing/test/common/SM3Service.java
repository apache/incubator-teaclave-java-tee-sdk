package com.alibaba.confidentialcomputing.test.common;

import com.alibaba.confidentialcomputing.common.annotations.EnclaveService;

@EnclaveService
public interface SM3Service {
    byte[] sm3Service(String plainText) throws Exception;
}
