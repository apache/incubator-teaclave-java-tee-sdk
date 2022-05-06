package com.alibaba.confidentialcomputing.test.enclave;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestSayHelloServiceImpl {

    @Test
    public void testSayHelloServiceImpl() {
        SayHelloServiceImpl service = new SayHelloServiceImpl();
        assertEquals("Hello World", service.sayHelloService("Hello World"));
    }
}