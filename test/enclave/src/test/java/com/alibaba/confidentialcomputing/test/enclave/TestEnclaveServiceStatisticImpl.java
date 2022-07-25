package com.alibaba.confidentialcomputing.test.enclave;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestEnclaveServiceStatisticImpl {
    @Test
    public void testEnclaveServiceStatisticImpl() throws Exception {
        assertEquals(0, new EnclaveServiceStatisticImpl().getEnclaveServiceCount());
    }
}
