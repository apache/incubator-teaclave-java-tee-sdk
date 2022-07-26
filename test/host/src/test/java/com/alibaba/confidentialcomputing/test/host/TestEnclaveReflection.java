package com.alibaba.confidentialcomputing.test.host;

import com.alibaba.confidentialcomputing.host.Enclave;
import com.alibaba.confidentialcomputing.host.EnclaveFactory;
import com.alibaba.confidentialcomputing.host.EnclaveType;
import com.alibaba.confidentialcomputing.host.exception.EnclaveCreatingException;
import com.alibaba.confidentialcomputing.host.exception.EnclaveDestroyingException;
import com.alibaba.confidentialcomputing.host.exception.ServicesLoadingException;
import com.alibaba.confidentialcomputing.test.common.ReflectionCallService;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestEnclaveReflection {

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

    @Test
    public void testReflectionCallService() throws Exception {
        reflectionCallService(EnclaveType.MOCK_IN_JVM);
        reflectionCallService(EnclaveType.MOCK_IN_SVM);
        reflectionCallService(EnclaveType.TEE_SDK);
        reflectionCallService(EnclaveType.EMBEDDED_LIB_OS);
    }
}
