package com.alibaba.confidentialcomputing.test.host;

import com.alibaba.confidentialcomputing.host.Enclave;
import com.alibaba.confidentialcomputing.host.EnclaveFactory;
import com.alibaba.confidentialcomputing.host.EnclaveType;
import com.alibaba.confidentialcomputing.test.common.EnclaveServiceStatistic;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class TestEnclaveServiceGC {
    private void enclaveServiceGC(EnclaveType type) throws Exception {
        int count = 1001;
        Enclave enclave = EnclaveFactory.create(type);
        assertNotNull(enclave);
        for (int i = 0x0; i < count; i++) {
            Iterator<EnclaveServiceStatistic> userServices = enclave.load(EnclaveServiceStatistic.class);
            assertNotNull(userServices);
            assertTrue(userServices.hasNext());
        }
        System.gc();
        Thread.sleep(1000);
        System.gc();
        Thread.sleep(1000);
        Iterator<EnclaveServiceStatistic> userServices = enclave.load(EnclaveServiceStatistic.class);
        assertEquals(1, userServices.next().getEnclaveServiceCount());
        enclave.destroy();
    }

    @Test
    public void testEnclaveServiceGC() throws Exception {
        enclaveServiceGC(EnclaveType.MOCK_IN_SVM);
        enclaveServiceGC(EnclaveType.TEE_SDK);
        enclaveServiceGC(EnclaveType.EMBEDDED_LIB_OS);
    }
}
