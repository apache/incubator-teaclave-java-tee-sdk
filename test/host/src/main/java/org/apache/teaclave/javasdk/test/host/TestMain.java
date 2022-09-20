package org.apache.teaclave.javasdk.test.host;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@org.junit.runners.Suite.SuiteClasses({
        TestEnclaveAES.class,
        TestEnclaveConcurrency.class,
        TestEnclaveException.class,
        TestEnclaveInfo.class,
        TestEnclaveInfoMXBean.class,
        TestEnclaveMetricTrace.class,
        TestEnclaveReflection.class,
        TestEnclaveRSA.class,
        TestEnclaveServiceGC.class,
        TestEnclaveSHA.class,
        TestHelloWorld.class,
        TestRemoteAttestation.class,
        TestSMEnclave.class
})
class TestJavaEnclaveSuites {
    //
}

public class TestMain {
    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(TestJavaEnclaveSuites.class);
        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }
        System.out.println("Teaclave java sdk ut result: " + result.wasSuccessful());
        System.exit(result.wasSuccessful() ? 0 : 1);
    }
}
