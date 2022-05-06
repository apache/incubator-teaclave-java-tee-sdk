package com.alibaba.confidentialcomputing.test.common;

import com.alibaba.confidentialcomputing.common.annotations.EnclaveService;

@EnclaveService
public interface EnclaveException {
    void enclaveException(String info) throws JavaEnclaveException;
}
