package com.alibaba.confidentialcomputing.test.common;

import com.alibaba.confidentialcomputing.common.annotations.EnclaveService;

@EnclaveService
public interface ConcurrencyCalculate {
    void add(int delta);
    long sum();
    void addSync(int delta);
    long sumSync();
}
