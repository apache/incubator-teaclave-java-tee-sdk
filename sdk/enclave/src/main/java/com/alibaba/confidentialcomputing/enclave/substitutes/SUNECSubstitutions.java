package com.alibaba.confidentialcomputing.enclave.substitutes;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import sun.security.ec.ECDHKeyAgreement;
import sun.security.ec.ECKeyPairGenerator;

import java.security.GeneralSecurityException;

public class SUNECSubstitutions {

    @TargetClass(ECKeyPairGenerator.class)
    public static final class Target_sun_security_ec_ECKeyPairGenerator {
        @Substitute
        static Object[] generateECKeyPair(int keySize,
                        byte[] encodedParams, byte[] seed) throws GeneralSecurityException {
            return NativeSunECMethods.generateECKeyPair(keySize, encodedParams, seed);
        }

        @Substitute
        static boolean isCurveSupported(byte[] encodedParams) {
            return NativeSunECMethods.isCurveSupported(encodedParams);
        }
    }

    @TargetClass(className = "sun.security.ec.ECDSASignature")
    public static final class Target_sun_security_ec_ECDSASignature {

        @Substitute
        static byte[] signDigest(byte[] digest, byte[] s,
                        byte[] encodedParams, byte[] seed, int timing)
                        throws GeneralSecurityException {
            return NativeSunECMethods.signDigest(digest, s, encodedParams, seed, timing);
        }

        @Substitute
        static boolean verifySignedDigest(byte[] signature,
                        byte[] digest, byte[] w, byte[] encodedParams)
                        throws GeneralSecurityException {
            return NativeSunECMethods.verifySignedDigest(signature, digest, w, encodedParams);
        }
    }

    @TargetClass(ECDHKeyAgreement.class)
    public static final class Target_sun_security_ec_ECDHKeyAgreement {
        @Substitute
        static byte[] deriveKey(byte[] s, byte[] w,
                        byte[] encodedParams) throws GeneralSecurityException {
            return NativeSunECMethods.deriveKey(s, w, encodedParams);
        }
    }

}
