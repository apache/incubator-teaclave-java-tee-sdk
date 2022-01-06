package com.alibaba.confidentialcomputing.host;

import com.alibaba.confidentialcomputing.host.exception.EnclaveCreatingException;
import com.alibaba.confidentialcomputing.host.exception.EnclaveDestroyingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestEnclaveFactory {
    @Test
    void testEnclaveCreate() throws EnclaveCreatingException, EnclaveDestroyingException {
        Enclave enclave = EnclaveFactory.create(EnclaveType.MOCK_IN_JVM);
        assertTrue(enclave instanceof MockInJvmEnclave);
        enclave.destroy();
    }
}
