package com.alibaba.samples.helloworld.common;

import com.alibaba.confidentialcomputing.common.annotations.EnclaveService;

@EnclaveService
public interface Service {
    String sayHelloWorld();
}
