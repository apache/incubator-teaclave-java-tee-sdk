package com.alibaba.confidentialcomputing.test.host;

import com.alibaba.confidentialcomputing.host.Enclave;
import com.alibaba.confidentialcomputing.host.EnclaveFactory;
import com.alibaba.confidentialcomputing.host.EnclaveType;
import com.alibaba.confidentialcomputing.test.common.SHAService;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class TestEnclaveSHA {
    private String encryptSHA(String plaintext, String SHAType) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(SHAType);
        byte[] messageDigest = md.digest(plaintext.getBytes());
        BigInteger no = new BigInteger(1, messageDigest);
        String hashtext = no.toString(16);
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        return hashtext;
    }

    @Test
    public void testEnclaveSHA() throws Exception {
        String plaintext = "Hello World!!!";
        EnclaveType[] types = new EnclaveType[]{
                EnclaveType.MOCK_IN_JVM,
                EnclaveType.MOCK_IN_SVM,
                EnclaveType.TEE_SDK,
                EnclaveType.EMBEDDED_LIB_OS};

        for (EnclaveType type : types) {
            Enclave enclave = EnclaveFactory.create(type);
            assertNotNull(enclave);
            Iterator<SHAService> userServices = enclave.load(SHAService.class);
            assertNotNull(userServices);
            assertTrue(userServices.hasNext());
            SHAService service = userServices.next();
            String result = service.encryptPlaintext(plaintext, "SHA-384");
            assertEquals(encryptSHA(plaintext, "SHA-384"), result);
            result = service.encryptPlaintext(plaintext, "SHA-512");
            assertEquals(encryptSHA(plaintext, "SHA-512"), result);
            enclave.destroy();
        }
    }
}
