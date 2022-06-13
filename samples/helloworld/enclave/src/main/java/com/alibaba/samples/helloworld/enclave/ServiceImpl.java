package com.alibaba.samples.helloworld.enclave;

import com.alibaba.samples.helloworld.common.Service;
import com.google.auto.service.AutoService;

@AutoService(Service.class)
public class ServiceImpl implements Service {
    @Override
    public String sayHelloWorld() {
        return "Hello World";
    }
}
