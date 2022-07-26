package com.alibaba.confidentialcomputing.test.enclave;

import com.alibaba.confidentialcomputing.test.common.AESSealedTest;
import com.alibaba.confidentialcomputing.test.common.AESService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestAESServiceImpl {
    String plaintext = "Hello World!!!";

    @Test
    public void testAESServiceImpl() throws Exception {
        AESService service = new AESServiceImpl();
        String result = service.aesEncryptAndDecryptPlaintext(plaintext);
        assertEquals(plaintext, result);
        result = service.aesEncryptAndDecryptPlaintextWithPassword(plaintext, "javaenclave", "12345678");
        assertEquals(plaintext, result);
        AESSealedTest obj = new AESSealedTest("javaenclave", 25, 5);
        assertEquals(0, obj.compareTo((AESSealedTest) service.aesEncryptAndDecryptObject(obj)));
    }
}
