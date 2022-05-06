package com.alibaba.confidentialcomputing.test.enclave;

import com.alibaba.confidentialcomputing.test.common.EnclaveException;
import com.alibaba.confidentialcomputing.test.common.JavaEnclaveException;
import com.google.auto.service.AutoService;

@AutoService(EnclaveException.class)
public class EnclaveExceptionImpl implements EnclaveException {
    @Override
    public void enclaveException(String info) throws JavaEnclaveException {
        throw new JavaEnclaveException(info);
    }
}
