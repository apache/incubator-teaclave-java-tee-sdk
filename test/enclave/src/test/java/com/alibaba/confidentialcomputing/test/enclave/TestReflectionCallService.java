package com.alibaba.confidentialcomputing.test.enclave;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestReflectionCallService {

    @Test
    public void testReflectionCallService() {
        ReflectionCallServiceImpl service = new ReflectionCallServiceImpl();
        assertEquals(20, service.add(2, 18));
        assertEquals(-20, service.sub(2, 22));
    }
}
