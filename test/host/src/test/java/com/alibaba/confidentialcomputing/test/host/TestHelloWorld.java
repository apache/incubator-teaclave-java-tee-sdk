package com.alibaba.confidentialcomputing.test.host;

import com.alibaba.confidentialcomputing.host.Enclave;
import com.alibaba.confidentialcomputing.host.EnclaveFactory;
import com.alibaba.confidentialcomputing.host.EnclaveType;
import com.alibaba.confidentialcomputing.host.exception.EnclaveCreatingException;
import com.alibaba.confidentialcomputing.host.exception.EnclaveDestroyingException;
import com.alibaba.confidentialcomputing.host.exception.RemoteAttestationException;
import com.alibaba.confidentialcomputing.host.exception.ServicesLoadingException;
import com.alibaba.confidentialcomputing.test.common.SayHelloService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class TestHelloWorld {

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

    @Test
    public void testSayHelloService() throws Exception {
        assertEquals("Hello World", sayHelloService(EnclaveType.MOCK_IN_JVM, "Hello World"));
        assertEquals("Hello World", sayHelloService(EnclaveType.MOCK_IN_SVM, "Hello World"));
        assertEquals("Hello World", sayHelloService(EnclaveType.TEE_SDK, "Hello World"));
        assertEquals("Hello World", sayHelloService(EnclaveType.EMBEDDED_LIB_OS, "Hello World"));
    }
}
