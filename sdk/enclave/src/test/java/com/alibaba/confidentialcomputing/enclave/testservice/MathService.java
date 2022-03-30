package com.alibaba.confidentialcomputing.enclave.testservice;

import com.alibaba.confidentialcomputing.common.annotations.EnclaveService;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@EnclaveService
public interface MathService<T> {
    T add(T x, T y);

    T minus(T x, T y);

    T div(T x, T y);

    default int getConstant() {
        return 100;
    }

    default byte[] getRandomNumber(int size) {
        SecureRandom secureRandom = null;
        try {
            secureRandom = SecureRandom.getInstance("NativePRNG");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return secureRandom.generateSeed(size);
    }
}
