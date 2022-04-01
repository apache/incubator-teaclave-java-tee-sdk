package com.alibaba.confidentialcomputing.enclave.testservice;

import com.alibaba.confidentialcomputing.common.annotations.EnclaveService;

@EnclaveService
public interface MemService {
    long getSize();
}
