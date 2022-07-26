package com.alibaba.confidentialcomputing.test.host;

import com.alibaba.confidentialcomputing.host.Enclave;
import com.alibaba.confidentialcomputing.host.EnclaveFactory;
import com.alibaba.confidentialcomputing.host.EnclaveType;
import com.alibaba.confidentialcomputing.test.common.AESSealedTest;
import com.alibaba.confidentialcomputing.test.common.AESService;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class TestEnclaveAES {
    @Test
    public void testAESService() throws Exception {
        String plaintext = "Hello World!!!";
        EnclaveType[] types = new EnclaveType[]{
                EnclaveType.MOCK_IN_JVM,
                EnclaveType.MOCK_IN_SVM,
                EnclaveType.TEE_SDK,
                EnclaveType.EMBEDDED_LIB_OS};

        for (EnclaveType type : types) {
            Enclave enclave = EnclaveFactory.create(type);
            assertNotNull(enclave);
            Iterator<AESService> userServices = enclave.load(AESService.class);
            assertNotNull(userServices);
            assertTrue(userServices.hasNext());
            AESService service = userServices.next();
            String result = service.aesEncryptAndDecryptPlaintext(plaintext);
            assertEquals(plaintext, result);
            result = service.aesEncryptAndDecryptPlaintextWithPassword(plaintext, "javaenclave", "12345678");
            assertEquals(plaintext, result);
            AESSealedTest obj = new AESSealedTest("javaenclave", 25, 5);
            assertEquals(0, obj.compareTo((AESSealedTest) service.aesEncryptAndDecryptObject(obj)));
            enclave.destroy();
        }
    }
}
