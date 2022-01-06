package com.alibaba.confidentialcomputing.host;

import com.alibaba.confidentialcomputing.common.ServiceHandler;
import com.alibaba.confidentialcomputing.host.exception.EnclaveCreatingException;
import com.alibaba.confidentialcomputing.host.exception.EnclaveMethodInvokingException;
import com.alibaba.confidentialcomputing.host.exception.ServicesLoadingException;
import org.junit.jupiter.api.*;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Iterator;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class TestAbstractEnclave {
    private static Enclave enclave;

    @BeforeAll
    static void create() throws EnclaveCreatingException {
        enclave = new MockTestEnclave();
    }

    @Test
    void testEnclave() throws Exception {
        Iterator<?> services = enclave.load(Service.class);
        assertEquals(1, ((MockTestEnclave) enclave).getServicesNum());
        assertNotNull(services);
        assertTrue(services.hasNext());
        Service service = (Service) services.next();
        service.doNothing();
        assertEquals(200, service.add(20, 180));
        assertTrue("Hello World".equals(service.saySomething("Hello World")));
        assertThrows(ServiceExceptionTest.class, () -> service.throwException("something is wrong"));
        Queue<?> queue = ((MockTestEnclave) enclave).getCachedServiceHandler();
        assertEquals(1, queue.size());
        ((MockTestEnclave) enclave).unloadService((ServiceHandler) queue.poll());
        assertEquals(0, ((MockTestEnclave) enclave).getServicesNum());
        enclave.destroy();
        assertThrows(ServicesLoadingException.class, () -> enclave.load(Service.class));
        try {
            service.doNothing();
        } catch (UndeclaredThrowableException e) {
            assertEquals(e.getCause().getClass(), EnclaveMethodInvokingException.class);
        }
    }
}
