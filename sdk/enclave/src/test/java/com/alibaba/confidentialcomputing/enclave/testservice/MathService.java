package com.alibaba.confidentialcomputing.enclave.testservice;

import com.alibaba.confidentialcomputing.common.annotations.EnclaveService;

@EnclaveService
public interface MathService<T> {
    T add(T x, T y);

    T minus(T x, T y);

    T div(T x, T y);

    default int getConstant(){
        return 100;
    }
}
