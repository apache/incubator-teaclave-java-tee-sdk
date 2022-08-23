package com.alibaba.confidentialcomputing.test.host;

import com.alibaba.confidentialcomputing.host.Enclave;
import com.alibaba.confidentialcomputing.host.EnclaveFactory;
import com.alibaba.confidentialcomputing.host.EnclaveType;
import com.alibaba.confidentialcomputing.test.common.ConcurrencyCalculate;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class TestEnclaveConcurrency {
    private void enclaveConcurrency(EnclaveType type) throws Exception {
        int concurrency = 10;
        int workload = 10_000;
        CountDownLatch latch0 = new CountDownLatch(1);
        CountDownLatch latch1 = new CountDownLatch(concurrency);

        Enclave enclave = EnclaveFactory.create(type);
        Iterator<ConcurrencyCalculate> services = enclave.load(ConcurrencyCalculate.class);
        assertTrue(services.hasNext());
        ConcurrencyCalculate service = services.next();
        for (int i = 0; i < concurrency; i++) {
            new Thread(() -> {
                try {
                    latch0.await();
                    for (int i1 = 0; i1 < workload; i1++) {
                        service.add(1);
                    }
                    latch1.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
        latch0.countDown();
        latch1.await();
        assertEquals(concurrency * workload, service.sum());

        // waiting for enclave service recycle.
        System.gc();
        Thread.sleep(2000);
        enclave.destroy();
    }

    private void enclaveConcurrencySync(EnclaveType type) throws Exception {
        int concurrency = 10;
        int workload = 20_000;
        CountDownLatch latch0 = new CountDownLatch(1);
        CountDownLatch latch1 = new CountDownLatch(concurrency);

        Enclave enclave = EnclaveFactory.create(type);
        Iterator<ConcurrencyCalculate> services = enclave.load(ConcurrencyCalculate.class);
        assertTrue(services.hasNext());
        ConcurrencyCalculate service = services.next();
        for (int i = 0; i < concurrency; i++) {
            new Thread(() -> {
                try {
                    latch0.await();
                    service.addSync(workload);
                    latch1.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
        latch0.countDown();
        latch1.await();
        assertEquals(concurrency * workload, service.sumSync());

        // waiting for enclave service recycle.
        System.gc();
        Thread.sleep(2000);
        enclave.destroy();
    }

    @Test
    public void testEnclaveConcurrency() throws Exception {
        enclaveConcurrency(EnclaveType.MOCK_IN_JVM);
        enclaveConcurrency(EnclaveType.MOCK_IN_SVM);
        enclaveConcurrency(EnclaveType.TEE_SDK);
        enclaveConcurrency(EnclaveType.EMBEDDED_LIB_OS);
    }

    @Test
    public void testEnclaveConcurrencySync() throws Exception {
        enclaveConcurrencySync(EnclaveType.MOCK_IN_JVM);
        enclaveConcurrencySync(EnclaveType.MOCK_IN_SVM);
        enclaveConcurrencySync(EnclaveType.TEE_SDK);
        enclaveConcurrencySync(EnclaveType.EMBEDDED_LIB_OS);
    }
}
