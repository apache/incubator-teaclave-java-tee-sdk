package com.alibaba.confidentialcomputing.test.common;

import com.alibaba.confidentialcomputing.common.annotations.EnclaveService;

@EnclaveService
public interface SayHelloService {
    String sayHelloService(String plainText);
    String sayHelloWorld();
}