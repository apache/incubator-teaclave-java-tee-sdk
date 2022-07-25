package com.alibaba.confidentialcomputing.test.common;

import com.alibaba.confidentialcomputing.common.annotations.EnclaveService;

@EnclaveService
public interface EnclaveServiceStatistic {
    int getEnclaveServiceCount() throws Exception;
}
