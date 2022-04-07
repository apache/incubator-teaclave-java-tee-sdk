package com.alibaba.confidentialcomputing.enclave.testservice;

import java.security.KeyPair;
import sun.security.ec.ECKeyPairGenerator;

public class SunECOperations implements EncryptionService{
    @Override
    public KeyPair generateKeyPair() {
        ECKeyPairGenerator pairGenerator = new ECKeyPairGenerator();
        return pairGenerator.generateKeyPair();
    }
}
