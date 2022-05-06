package com.alibaba.confidentialcomputing.test.enclave;

import com.alibaba.confidentialcomputing.test.common.JavaEnclaveException;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestEnclaveException {
    @Test
    public void testEnclaveException() {
        assertThrows(JavaEnclaveException.class, () -> new EnclaveExceptionImpl().enclaveException("JavaEnclave Exception"));
    }
}
