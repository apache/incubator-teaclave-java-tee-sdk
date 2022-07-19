package com.alibaba.confidentialcomputing.test.host;

import java.io.IOException;
import java.util.Iterator;

import com.alibaba.confidentialcomputing.host.*;
import com.alibaba.confidentialcomputing.host.exception.EnclaveCreatingException;
import com.alibaba.confidentialcomputing.host.exception.EnclaveDestroyingException;
import com.alibaba.confidentialcomputing.host.exception.RemoteAttestationException;
import com.alibaba.confidentialcomputing.host.exception.ServicesLoadingException;
import com.alibaba.confidentialcomputing.test.common.EnclaveException;
import com.alibaba.confidentialcomputing.test.common.JavaEnclaveException;
import com.alibaba.confidentialcomputing.test.common.ReflectionCallService;
import com.alibaba.confidentialcomputing.test.common.SayHelloService;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestJavaEnclaveService {
    private String sayHelloService(EnclaveType type, String plain) throws
            EnclaveCreatingException, ServicesLoadingException, EnclaveDestroyingException, RemoteAttestationException, IOException {
        Enclave enclave = EnclaveFactory.create(type);
        if (type == EnclaveType.TEE_SDK) {
            TeeSdkAttestationReport report = (TeeSdkAttestationReport) RemoteAttestation.generateAttestationReport(enclave, null);
            assertEquals(report.getEnclaveType(), EnclaveType.TEE_SDK);
            assertNotNull(report.getQuote());
            assertEquals(0, RemoteAttestation.verifyAttestationReport(report));
            assertNotNull(report.getMeasurementEnclave());
            assertNotNull(report.getMeasurementSigner());
        }
        Iterator<SayHelloService> userServices = enclave.load(SayHelloService.class);
        assertNotNull(userServices);
        assertTrue(userServices.hasNext());
        SayHelloService service = userServices.next();
        String result = service.sayHelloService(plain);
        assertEquals("Hello World", service.sayHelloWorld());
        enclave.destroy();
        return result;
    }

    private void reflectionCallService(EnclaveType type) throws EnclaveCreatingException, ServicesLoadingException, EnclaveDestroyingException, RemoteAttestationException {
        Enclave enclave = EnclaveFactory.create(type);
        if (type == EnclaveType.TEE_SDK) {
            TeeSdkAttestationReport report = (TeeSdkAttestationReport) RemoteAttestation.generateAttestationReport(enclave, null);
            assertEquals(report.getEnclaveType(), EnclaveType.TEE_SDK);
            assertNotNull(report.getQuote());
            assertEquals(0, RemoteAttestation.verifyAttestationReport(report));
            assertNotNull(report.getMeasurementEnclave());
            assertNotNull(report.getMeasurementSigner());
        }
        Iterator<ReflectionCallService> userServices = enclave.load(ReflectionCallService.class);
        assertNotNull(userServices);
        assertTrue(userServices.hasNext());
        ReflectionCallService service = userServices.next();
        assertEquals(20, service.add(2, 18));
        assertEquals(-20, service.sub(2, 22));
        enclave.destroy();
    }

    private void javaEnclaveException(EnclaveType type) throws EnclaveCreatingException, ServicesLoadingException, EnclaveDestroyingException, RemoteAttestationException {
        Enclave enclave = EnclaveFactory.create(type);
        if (type == EnclaveType.TEE_SDK) {
            TeeSdkAttestationReport report = (TeeSdkAttestationReport) RemoteAttestation.generateAttestationReport(enclave, null);
            assertEquals(report.getEnclaveType(), EnclaveType.TEE_SDK);
            assertNotNull(report.getQuote());
            assertEquals(0, RemoteAttestation.verifyAttestationReport(report));
            assertNotNull(report.getMeasurementEnclave());
            assertNotNull(report.getMeasurementSigner());
        }
        Iterator<EnclaveException> userServices = enclave.load(EnclaveException.class);
        assertNotNull(userServices);
        assertTrue(userServices.hasNext());
        EnclaveException service = userServices.next();
        assertThrows(JavaEnclaveException.class, () -> service.enclaveException("JavaEnclave Exception"));
        enclave.destroy();
    }

    @Test
    public void testSayHelloService() throws
            EnclaveCreatingException, EnclaveDestroyingException, ServicesLoadingException, RemoteAttestationException, IOException {
        assertEquals("Hello World", sayHelloService(EnclaveType.MOCK_IN_JVM, "Hello World"));
        assertEquals("Hello World", sayHelloService(EnclaveType.MOCK_IN_SVM, "Hello World"));
        assertEquals("Hello World", sayHelloService(EnclaveType.TEE_SDK, "Hello World"));
    }

    @Test
    public void testReflectionCallService() throws ServicesLoadingException, EnclaveCreatingException, EnclaveDestroyingException, RemoteAttestationException {
        reflectionCallService(EnclaveType.MOCK_IN_JVM);
        reflectionCallService(EnclaveType.MOCK_IN_SVM);
        reflectionCallService(EnclaveType.TEE_SDK);
    }

    @Test
    public void testJavaEnclaveException() throws ServicesLoadingException, EnclaveCreatingException, EnclaveDestroyingException, RemoteAttestationException {
        javaEnclaveException(EnclaveType.MOCK_IN_JVM);
        javaEnclaveException(EnclaveType.MOCK_IN_SVM);
        javaEnclaveException(EnclaveType.TEE_SDK);
    }
}
