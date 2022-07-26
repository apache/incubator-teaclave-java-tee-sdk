package com.alibaba.confidentialcomputing.test.enclave;

import com.alibaba.confidentialcomputing.test.common.SHAService;
import com.google.auto.service.AutoService;

import java.math.BigInteger;
import java.security.MessageDigest;

@AutoService(SHAService.class)
public class SHAServiceImpl implements SHAService {
    @Override
    public String encryptPlaintext(String plaintext, String SHAType) throws Exception {
        MessageDigest md = MessageDigest.getInstance(SHAType);
        byte[] messageDigest = md.digest(plaintext.getBytes());
        BigInteger no = new BigInteger(1, messageDigest);
        String hashtext = no.toString(16);
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        return hashtext;
    }
}
