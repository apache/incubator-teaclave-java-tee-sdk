package com.alibaba.confidentialcomputing.test.host;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

import com.alibaba.confidentialcomputing.host.*;
import com.alibaba.confidentialcomputing.host.exception.EnclaveCreatingException;
import com.alibaba.confidentialcomputing.host.exception.EnclaveDestroyingException;
import com.alibaba.confidentialcomputing.host.exception.RemoteAttestationException;
import com.alibaba.confidentialcomputing.host.exception.ServicesLoadingException;
import com.alibaba.confidentialcomputing.test.common.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestJavaEnclaveService {
    private String sayHelloService(EnclaveType type, String plain) throws
            EnclaveCreatingException, ServicesLoadingException, EnclaveDestroyingException, RemoteAttestationException, IOException {
        Enclave enclave = EnclaveFactory.create(type);
        assertNotNull(enclave);
        Iterator<SayHelloService> userServices = enclave.load(SayHelloService.class);
        assertNotNull(userServices);
        assertTrue(userServices.hasNext());
        SayHelloService service = userServices.next();
        String result = service.sayHelloService(plain);
        assertEquals("Hello World", service.sayHelloWorld());
        enclave.destroy();
        return result;
    }

    private void reflectionCallService(EnclaveType type) throws EnclaveCreatingException, ServicesLoadingException, EnclaveDestroyingException {
        Enclave enclave = EnclaveFactory.create(type);
        assertNotNull(enclave);
        Iterator<ReflectionCallService> userServices = enclave.load(ReflectionCallService.class);
        assertNotNull(userServices);
        assertTrue(userServices.hasNext());
        ReflectionCallService service = userServices.next();
        assertEquals(20, service.add(2, 18));
        assertEquals(-20, service.sub(2, 22));
        enclave.destroy();
    }

    private void javaEnclaveException(EnclaveType type) throws EnclaveCreatingException, ServicesLoadingException, EnclaveDestroyingException {
        Enclave enclave = EnclaveFactory.create(type);
        assertNotNull(enclave);
        Iterator<EnclaveException> userServices = enclave.load(EnclaveException.class);
        assertNotNull(userServices);
        assertTrue(userServices.hasNext());
        EnclaveException service = userServices.next();
        assertThrows(JavaEnclaveException.class, () -> service.enclaveException("JavaEnclave Exception"));
        enclave.destroy();
    }

    private void remoteAttestation(EnclaveType type) throws EnclaveCreatingException, RemoteAttestationException, EnclaveDestroyingException {
        Enclave enclave = EnclaveFactory.create(type);
        assertNotNull(enclave);
        byte[] userData = new byte[64];
        new Random().nextBytes(userData);

        SGXAttestationReport report = (SGXAttestationReport) RemoteAttestation.generateAttestationReport(enclave, userData);
        assertEquals(report.getEnclaveType(), type);
        assertNotNull(report.getQuote());
        assertEquals(0, RemoteAttestation.verifyAttestationReport(report));
        assertNotNull(report.getMeasurementEnclave());
        assertNotNull(report.getMeasurementSigner());
        assertNotNull(report.getUserData());
        assertArrayEquals(userData, report.getUserData());
        enclave.destroy();
    }

    private void enclaveServiceGC(EnclaveType type) throws Exception {
        int count = 10001;
        Enclave enclave = EnclaveFactory.create(type);
        assertNotNull(enclave);
        for (int i = 0x0; i < count; i++) {
            Iterator<EnclaveServiceStatistic> userServices = enclave.load(EnclaveServiceStatistic.class);
            assertNotNull(userServices);
            assertTrue(userServices.hasNext());
        }
        System.gc();
        Thread.sleep(2000);
        System.gc();
        Thread.sleep(2000);
        Iterator<EnclaveServiceStatistic> userServices = enclave.load(EnclaveServiceStatistic.class);
        assertEquals(1, userServices.next().getEnclaveServiceCount());
        enclave.destroy();
    }

    @Test
    public void testSayHelloService() throws Exception {
        assertEquals("Hello World", sayHelloService(EnclaveType.MOCK_IN_JVM, "Hello World"));
        assertEquals("Hello World", sayHelloService(EnclaveType.MOCK_IN_SVM, "Hello World"));
        assertEquals("Hello World", sayHelloService(EnclaveType.TEE_SDK, "Hello World"));
        assertEquals("Hello World", sayHelloService(EnclaveType.EMBEDDED_LIB_OS, "Hello World"));
    }

    @Test
    public void testReflectionCallService() throws Exception {
        reflectionCallService(EnclaveType.MOCK_IN_JVM);
        reflectionCallService(EnclaveType.MOCK_IN_SVM);
        reflectionCallService(EnclaveType.TEE_SDK);
        reflectionCallService(EnclaveType.EMBEDDED_LIB_OS);
    }

    @Test
    public void testJavaEnclaveException() throws Exception {
        javaEnclaveException(EnclaveType.MOCK_IN_JVM);
        javaEnclaveException(EnclaveType.MOCK_IN_SVM);
        javaEnclaveException(EnclaveType.TEE_SDK);
        javaEnclaveException(EnclaveType.EMBEDDED_LIB_OS);
    }

    @Test
    public void testRemoteAttestation() throws Exception {
        remoteAttestation(EnclaveType.TEE_SDK);
        remoteAttestation(EnclaveType.EMBEDDED_LIB_OS);
    }

    @Test
    public void testEnclaveServiceGC() throws Exception {
        enclaveServiceGC(EnclaveType.MOCK_IN_SVM);
        enclaveServiceGC(EnclaveType.TEE_SDK);
        enclaveServiceGC(EnclaveType.EMBEDDED_LIB_OS);
    }
}
