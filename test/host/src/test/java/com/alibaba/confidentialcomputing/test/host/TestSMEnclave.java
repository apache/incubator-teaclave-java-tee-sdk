package com.alibaba.confidentialcomputing.test.host;

import com.alibaba.confidentialcomputing.host.Enclave;
import com.alibaba.confidentialcomputing.host.EnclaveFactory;
import com.alibaba.confidentialcomputing.host.EnclaveType;
import com.alibaba.confidentialcomputing.test.common.SM2Service;
import com.alibaba.confidentialcomputing.test.common.SM3Service;
import com.alibaba.confidentialcomputing.test.common.SM4Service;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class TestSMEnclave {
    private byte[] sm3Digest(String plaintext) {
        byte[] messages = plaintext.getBytes();
        Digest md = new SM3Digest();
        md.update(messages, 0, messages.length);
        byte[] digest = new byte[md.getDigestSize()];
        md.doFinal(digest, 0);
        return digest;
    }

    @Test
    public void testSM2Enclave() throws Exception {
        String plaintext = "Hello World!!!";
        EnclaveType[] types = new EnclaveType[]{
                EnclaveType.MOCK_IN_JVM,
                EnclaveType.MOCK_IN_SVM,
                EnclaveType.TEE_SDK,
                EnclaveType.EMBEDDED_LIB_OS};

        for (EnclaveType type : types) {
            Enclave enclave = EnclaveFactory.create(type);
            assertNotNull(enclave);
            Iterator<SM2Service> userServices = enclave.load(SM2Service.class);
            assertNotNull(userServices);
            assertTrue(userServices.hasNext());
            SM2Service service = userServices.next();
            String result = service.encryptAndDecryptWithPlaintext(plaintext);
            assertEquals(plaintext, result);
            enclave.destroy();
        }
    }

    @Test
    public void testSM3Enclave() throws Exception {
        String plaintext = "Hello World!!!";
        EnclaveType[] types = new EnclaveType[]{
                EnclaveType.MOCK_IN_JVM,
                EnclaveType.MOCK_IN_SVM,
                EnclaveType.TEE_SDK,
                EnclaveType.EMBEDDED_LIB_OS};

        for (EnclaveType type : types) {
            Enclave enclave = EnclaveFactory.create(type);
            assertNotNull(enclave);
            Iterator<SM3Service> userServices = enclave.load(SM3Service.class);
            assertNotNull(userServices);
            assertTrue(userServices.hasNext());
            SM3Service service = userServices.next();
            byte[] result = service.sm3Service(plaintext);
            assertArrayEquals(sm3Digest(plaintext), result);
            enclave.destroy();
        }
    }

    @Test
    public void testSM4Enclave() throws Exception {
        String plaintext = "Hello World!!!";
        EnclaveType[] types = new EnclaveType[]{
                EnclaveType.MOCK_IN_JVM,
                EnclaveType.MOCK_IN_SVM,
                EnclaveType.TEE_SDK,
                EnclaveType.EMBEDDED_LIB_OS};

        for (EnclaveType type : types) {
            Enclave enclave = EnclaveFactory.create(type);
            assertNotNull(enclave);
            Iterator<SM4Service> userServices = enclave.load(SM4Service.class);
            assertNotNull(userServices);
            assertTrue(userServices.hasNext());
            SM4Service service = userServices.next();
            assertEquals(service.sm4Service(plaintext), plaintext);
            enclave.destroy();
        }
    }
}
