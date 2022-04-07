package com.alibaba.confidentialcomputing.enclave.substitutes;

import java.security.GeneralSecurityException;

public class NativeSunECMethods {
    public static native boolean isCurveSupported(byte[] encodedParams);

    public static native Object[] generateECKeyPair(int keySize,
                    byte[] encodedParams, byte[] seed) throws GeneralSecurityException;

    public static native byte[] signDigest(byte[] digest, byte[] s,
                    byte[] encodedParams, byte[] seed, int timing)
                    throws GeneralSecurityException;

    public static native boolean verifySignedDigest(byte[] signature,
                    byte[] digest, byte[] w, byte[] encodedParams)
                    throws GeneralSecurityException;

    public static native byte[] deriveKey(byte[] s, byte[] w,
                    byte[] encodedParams) throws GeneralSecurityException;
}
