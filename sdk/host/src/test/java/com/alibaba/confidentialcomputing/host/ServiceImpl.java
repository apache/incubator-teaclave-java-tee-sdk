package com.alibaba.confidentialcomputing.host;

import com.google.auto.service.AutoService;

@AutoService(Service.class)
public class ServiceImpl implements Service {
    @Override
    public void doNothing() {
        // Do nothing;
    }

    @Override
    public int add(int a, int b) {
        return a + b;
    }

    @Override
    public String saySomething(String words) {
        return words;
    }

    @Override
    public void throwException(String code) throws ServiceExceptionTest {
        throw new ServiceExceptionTest(code);
    }
}
