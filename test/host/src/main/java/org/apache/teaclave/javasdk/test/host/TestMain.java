// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.teaclave.javasdk.test.host;

import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Suite;

// TestRemoteAttestation.class was removed becaused it's related to
// execution environment. PCCS_URL should be set correctly.
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
        TestSMEnclave.class
})
class TestJavaEnclaveSuites {
    //
}

public class TestMain {
    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        Result result = junit.run(TestJavaEnclaveSuites.class);
        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }
        System.out.println("Teaclave java sdk ut result: " + result.wasSuccessful());
        System.exit(result.wasSuccessful() ? 0 : 1);
    }
}
