package com.alibaba.confidentialcomputing.test.host;

import com.alibaba.confidentialcomputing.host.Enclave;
import com.alibaba.confidentialcomputing.host.EnclaveFactory;
import com.alibaba.confidentialcomputing.host.EnclaveType;
import com.alibaba.confidentialcomputing.host.exception.EnclaveCreatingException;
import com.alibaba.confidentialcomputing.host.exception.EnclaveDestroyingException;
import com.alibaba.confidentialcomputing.host.exception.ServicesLoadingException;
import com.alibaba.confidentialcomputing.test.common.EnclaveException;
import com.alibaba.confidentialcomputing.test.common.JavaEnclaveException;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class TestEnclaveException {

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

    @Test
    public void testJavaEnclaveException() throws Exception {
        javaEnclaveException(EnclaveType.MOCK_IN_JVM);
        javaEnclaveException(EnclaveType.MOCK_IN_SVM);
        javaEnclaveException(EnclaveType.TEE_SDK);
        javaEnclaveException(EnclaveType.EMBEDDED_LIB_OS);
    }
}
