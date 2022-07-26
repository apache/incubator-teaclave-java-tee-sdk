package com.alibaba.confidentialcomputing.test.host;

import com.alibaba.confidentialcomputing.host.Enclave;
import com.alibaba.confidentialcomputing.host.EnclaveFactory;
import com.alibaba.confidentialcomputing.host.EnclaveType;
import com.alibaba.confidentialcomputing.test.common.RSAService;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestEnclaveRSA {
    @Test
    public void testRSAService() throws Exception {
        String plaintext = "Hello World!!!";
        EnclaveType[] types = new EnclaveType[]{
                EnclaveType.MOCK_IN_JVM,
                EnclaveType.MOCK_IN_SVM,
                EnclaveType.TEE_SDK,
                EnclaveType.EMBEDDED_LIB_OS};

        for (EnclaveType type : types) {
            Enclave enclave = EnclaveFactory.create(type);
            assertNotNull(enclave);
            Iterator<RSAService> userServices = enclave.load(RSAService.class);
            assertNotNull(userServices);
            assertTrue(userServices.hasNext());
            RSAService service = userServices.next();
            String result = service.encryptAndDecryptWithPlaintext(plaintext);
            assertEquals(plaintext, result);
            enclave.destroy();
        }
    }
}
