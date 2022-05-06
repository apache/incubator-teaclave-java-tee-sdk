package com.alibaba.confidentialcomputing.test.common;

import com.alibaba.confidentialcomputing.common.annotations.EnclaveService;

@EnclaveService
public interface ReflectionCallService {
    int add(int a, int b);

    int sub(int a, int b);
}
