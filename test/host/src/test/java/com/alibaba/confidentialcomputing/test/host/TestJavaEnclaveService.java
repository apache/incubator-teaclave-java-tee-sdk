package com.alibaba.confidentialcomputing.test.host;

import java.util.Iterator;

import com.alibaba.confidentialcomputing.host.exception.EnclaveCreatingException;
import com.alibaba.confidentialcomputing.host.exception.EnclaveDestroyingException;
import com.alibaba.confidentialcomputing.host.exception.ServicesLoadingException;
import com.alibaba.confidentialcomputing.test.common.EnclaveException;
import com.alibaba.confidentialcomputing.test.common.JavaEnclaveException;
import com.alibaba.confidentialcomputing.test.common.ReflectionCallService;
import com.alibaba.confidentialcomputing.test.common.SayHelloService;
import com.alibaba.confidentialcomputing.host.Enclave;
import com.alibaba.confidentialcomputing.host.EnclaveFactory;
import com.alibaba.confidentialcomputing.host.EnclaveType;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestJavaEnclaveService {
    private String sayHelloService(EnclaveType type, String plain) throws
            EnclaveCreatingException, ServicesLoadingException, EnclaveDestroyingException {
        Enclave enclave = EnclaveFactory.create(type);
        Iterator<SayHelloService> userServices = enclave.load(SayHelloService.class);
        assertNotNull(userServices);
        assertTrue(userServices.hasNext());
        SayHelloService service = userServices.next();
        String result = service.sayHelloService(plain);
        enclave.destroy();
        return result;
    }

    private void reflectionCallService(EnclaveType type) throws EnclaveCreatingException, ServicesLoadingException, EnclaveDestroyingException {
        Enclave enclave = EnclaveFactory.create(type);
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
        Iterator<EnclaveException> userServices = enclave.load(EnclaveException.class);
        assertNotNull(userServices);
        assertTrue(userServices.hasNext());
        EnclaveException service = userServices.next();
        assertThrows(JavaEnclaveException.class, () -> service.enclaveException("JavaEnclave Exception"));
        enclave.destroy();
    }

    @Test
    public void testSayHelloService() throws
            EnclaveCreatingException, EnclaveDestroyingException, ServicesLoadingException {
        assertEquals("Hello World", sayHelloService(EnclaveType.MOCK_IN_JVM, "Hello World"));
        assertEquals("Hello World", sayHelloService(EnclaveType.MOCK_IN_SVM, "Hello World"));
        assertEquals("Hello World", sayHelloService(EnclaveType.TEE_SDK, "Hello World"));
    }

    @Test
    public void testReflectionCallService() throws ServicesLoadingException, EnclaveCreatingException, EnclaveDestroyingException {
        reflectionCallService(EnclaveType.MOCK_IN_JVM);
        reflectionCallService(EnclaveType.MOCK_IN_SVM);
        reflectionCallService(EnclaveType.TEE_SDK);
    }

    @Test
    public void testJavaEnclaveException() throws ServicesLoadingException, EnclaveCreatingException, EnclaveDestroyingException {
        javaEnclaveException(EnclaveType.MOCK_IN_JVM);
        javaEnclaveException(EnclaveType.MOCK_IN_SVM);
        javaEnclaveException(EnclaveType.TEE_SDK);
    }
}
