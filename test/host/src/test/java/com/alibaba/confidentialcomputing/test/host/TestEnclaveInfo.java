package com.alibaba.confidentialcomputing.test.host;

import com.alibaba.confidentialcomputing.host.Enclave;
import com.alibaba.confidentialcomputing.host.EnclaveFactory;
import com.alibaba.confidentialcomputing.host.EnclaveInfo;
import com.alibaba.confidentialcomputing.host.EnclaveType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestEnclaveInfo {

    @Test
    public void testEnclaveInfo() throws Exception {
        Enclave enclave = EnclaveFactory.create(EnclaveType.MOCK_IN_JVM);
        EnclaveInfo enclaveInfo = enclave.getEnclaveInfo();
        assertEquals(enclaveInfo.getEnclaveType(), EnclaveType.MOCK_IN_JVM);
        assertTrue(enclaveInfo.isEnclaveDebuggable());
        assertEquals(enclaveInfo.getEnclaveEPCMemorySizeBytes(), -1);
        assertEquals(enclaveInfo.getEnclaveMaxThreadsNumber(), -1);
        enclave.destroy();

        enclave = EnclaveFactory.create(EnclaveType.MOCK_IN_SVM);
        enclaveInfo = enclave.getEnclaveInfo();
        assertEquals(enclaveInfo.getEnclaveType(), EnclaveType.MOCK_IN_SVM);
        assertTrue(enclaveInfo.isEnclaveDebuggable());
        assertEquals(enclaveInfo.getEnclaveEPCMemorySizeBytes(), -1);
        assertEquals(enclaveInfo.getEnclaveMaxThreadsNumber(), -1);
        enclave.destroy();

        // it's related to config file in test project.
        enclave = EnclaveFactory.create(EnclaveType.TEE_SDK);
        enclaveInfo = enclave.getEnclaveInfo();
        assertEquals(enclaveInfo.getEnclaveType(), EnclaveType.TEE_SDK);
        assertFalse(enclaveInfo.isEnclaveDebuggable());
        assertEquals(enclaveInfo.getEnclaveEPCMemorySizeBytes(), 1500 * 1024 * 1024);
        assertEquals(enclaveInfo.getEnclaveMaxThreadsNumber(), 50);
        enclave.destroy();

        // it's related to config file in test project.
        enclave = EnclaveFactory.create(EnclaveType.EMBEDDED_LIB_OS);
        enclaveInfo = enclave.getEnclaveInfo();
        assertEquals(enclaveInfo.getEnclaveType(), EnclaveType.EMBEDDED_LIB_OS);
        assertFalse(enclaveInfo.isEnclaveDebuggable());
        assertEquals(enclaveInfo.getEnclaveEPCMemorySizeBytes(), 1500 * 1024 * 1024);
        assertEquals(enclaveInfo.getEnclaveMaxThreadsNumber(), 50);
        enclave.destroy();
    }
}
