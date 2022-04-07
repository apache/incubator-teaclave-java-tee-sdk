package com.alibaba.confidentialcomputing.enclave;

import com.alibaba.confidentialcomputing.enclave.testservice.EncryptionService;
import com.alibaba.confidentialcomputing.enclave.testservice.SunECOperations;
import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.hosted.FeatureImpl;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeSerialization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sun.security.ec.ECKeyPairGenerator;

import java.security.KeyPair;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ReplaceSunECTest {

    private static final String ENCRYPT_SERVICE = "com.alibaba.confidentialcomputing.enclave.testservice.EncryptionService";
    private static final String SUNEC_OP = "com.alibaba.confidentialcomputing.enclave.testservice.SunECOperations";
    @TestTarget(ReplaceSunECTest.class)
    static class ReplaceSunECPreparation extends NativeImageTest{

        @Override
        SVMCompileElements specifyTestClasses() {
            SVMCompileElements ret = new SVMCompileElements();
            // Specify the service file
            ret.addServices("META-INF/services/"+ENCRYPT_SERVICE);
            ret.addClasses(EncryptionService.class, SunECOperations.class, UTFeature.class);
            return ret;
        }

/*        @Override
        public List<String> extraSVMOptions() {
            return List.of("--debug-attach:5005");
        }*/
    }

    @BeforeAll
    public static void prepareLibraries(){
        new ReplaceSunECPreparation().prepareNativeLibraries();
    }

    @BeforeEach
    public void setup() {
        EnclaveTestHelper.createIsolate();
    }

    @AfterEach
    public void teardown() {
        EnclaveTestHelper.destroyIsolate();
    }

    @Test
    public void test(){
        String id = EnclaveTestHelper.loadAndGetService(ENCRYPT_SERVICE, SUNEC_OP, 1);
        KeyPair remoteRet = (KeyPair)EnclaveTestHelper.call(id, ENCRYPT_SERVICE, SUNEC_OP, "generateKeyPair", EnclaveTestHelper.EMPTY_STRING_ARRAY, EnclaveTestHelper.EMPTY_OBJECT_ARRAY);
        assertNotNull(remoteRet);
    }

    @AutomaticFeature
    static class UTFeature implements Feature {

        @Override
        public void beforeAnalysis(BeforeAnalysisAccess access) {
            FeatureImpl.BeforeAnalysisAccessImpl a = (FeatureImpl.BeforeAnalysisAccessImpl)access;
            Class<?> PKCS8KeyClass = a.getImageClassLoader().findClass("sun.security.pkcs.PKCS8Key").get();
            Class<?> X509KeyClass = a.getImageClassLoader().findClass("sun.security.x509.X509Key").get();
            RuntimeSerialization.register(PKCS8KeyClass, X509KeyClass);
            RuntimeSerialization.registerAllAssociatedClasses(java.security.KeyRep.class);
            RuntimeSerialization.registerAllAssociatedClasses(sun.security.ec.ECPrivateKeyImpl.class);
            RuntimeSerialization.registerAllAssociatedClasses(sun.security.ec.ECPublicKeyImpl.class);
        }
    }
}
