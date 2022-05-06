package com.alibaba.confidentialcomputing.test.enclave;

import com.alibaba.confidentialcomputing.test.common.SayHelloService;

import com.google.auto.service.AutoService;

@AutoService(SayHelloService.class)
public class SayHelloServiceImpl implements SayHelloService {
    @Override
    public String sayHelloService(String plainText) {
        return plainText;
    }
}