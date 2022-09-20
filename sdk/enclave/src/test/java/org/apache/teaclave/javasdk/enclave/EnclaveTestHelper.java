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

package org.apache.teaclave.javasdk.enclave;

import org.apache.teaclave.javasdk.common.EnclaveInvocationContext;
import org.apache.teaclave.javasdk.common.EnclaveInvocationResult;
import org.apache.teaclave.javasdk.common.SerializationHelper;
import org.apache.teaclave.javasdk.common.ServiceHandler;
import org.apache.teaclave.javasdk.enclave.testservice.IntegerMath;
import org.apache.teaclave.javasdk.enclave.testservice.MathService;
import org.apache.teaclave.javasdk.enclave.testservice.NumericMath;
import org.apache.teaclave.javasdk.enclave.testservice.PointMath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

public class EnclaveTestHelper {
    public static final String MATH_SERVICE = MathService.class.getName();
    public static final String NUMERIC_MATH = NumericMath.class.getName();
    public static final String INTEGER_MATH = IntegerMath.class.getName();
    public static final String POINT_MATH = PointMath.class.getName();
    public static final String[] MATH_ADD_PARAM_TYPES = {"java.lang.Number", "java.lang.Number"};
    public static final String[] POINT_MATH_ADD_PARAM_TYPES = {"org.apache.teaclave.javasdk.enclave.testservice.Point",
            "org.apache.teaclave.javasdk.enclave.testservice.Point"};
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    public static final String VM_NAME = System.getProperty("java.vm.name");

    public static native byte[] invokeEnclave(byte[] data);

    public static native byte[] loadService(byte[] data);

    public static native byte[] unloadService(byte[] data);

    public static native void createIsolate();

    public static native void createIsolate(String... argv);

    public static native void destroyIsolate();

    public static boolean isInNativeImage() {
        return VM_NAME.equals("Substrate VM");
    }

    public static ServiceHandler[] callLoadService(String serviceName) throws IOException, ClassNotFoundException {
        EnclaveInvocationResult ret = callEnclaveJNI(serviceName,
                input -> loadService(input));
        if (ret.getException() != null) {
            ret.getException().printStackTrace();
            fail();
        }
        return (ServiceHandler[]) ret.getResult();
    }

    public static EnclaveInvocationResult callEnclaveMethod(String service, String impl, String identity, String method, String[] paramTypes, Object[] values) throws IOException, ClassNotFoundException {
        EnclaveInvocationContext enclaveInvocationContext = new EnclaveInvocationContext(new ServiceHandler(service, impl
                , identity), method, paramTypes, values);
        return callEnclaveJNI(enclaveInvocationContext,
                input -> invokeEnclave(input));
    }

    private static EnclaveInvocationResult callEnclaveJNI(Object input, Function<byte[], byte[]> function) throws IOException, ClassNotFoundException {
        byte[] data = SerializationHelper.serialize(input);
        byte[] ret = function.apply(data);
        assertNotNull(ret, "The returned value must not be null.");
        return (EnclaveInvocationResult) SerializationHelper.deserialize(ret);
    }

    public static Path createTestTmpDir(Path root) {
        Path tmpDir;
        try {
            tmpDir = Files.createTempDirectory(root, "native-test-");
        } catch (IOException e) {
            e.printStackTrace();
            tmpDir = null;
        }
        return tmpDir;
    }

    public static String loadAndGetService(String serviceName, String implementation, int expectedServiceNum) {
        ServiceHandler[] serviceHandlers = new ServiceHandler[0];
        try {
            serviceHandlers = callLoadService(serviceName);
        } catch (IOException | ClassNotFoundException e) {
            fail(e);
        }
        assertEquals(expectedServiceNum, serviceHandlers.length);
        // SVM doesn't guarantee the service order
        for (ServiceHandler serviceHandler : serviceHandlers) {
            if (serviceHandler.getServiceImplClassName().equals(implementation)) {
                return serviceHandler.getInstanceIdentity();
            }
        }
        fail("Should not reach here");
        return null;
    }

    public static Object call(String id, String serviceName, String className, String methodName, String[] paramTypes, Object[] paramValues) {
        try {
            EnclaveInvocationResult result = callEnclaveMethod(serviceName,
                    className, id,
                    methodName,
                    paramTypes,
                    paramValues);
            Object wrappedResult = result.getResult();
            if (result.getException() != null) {
                result.getException().printStackTrace();
            }
            assertNotNull(wrappedResult, "Expect to have non-null result from invoking service method call.");
            assertNull(result.getException());
            return wrappedResult;
        } catch (IOException | ClassNotFoundException e) {
            fail(e);
            return 0;
        }
    }
}
