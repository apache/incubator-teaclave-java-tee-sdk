package com.alibaba.confidentialcomputing.host;

public interface Service {
    void doNothing();

    int add(int a, int b);

    String saySomething(String words);

    void throwException(String code) throws ServiceExceptionTest;
}
