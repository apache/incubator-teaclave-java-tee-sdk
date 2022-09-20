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
import org.apache.teaclave.javasdk.enclave.c.EnclaveEnvironment.CallBacks;
import org.apache.teaclave.javasdk.enclave.c.EnclaveEnvironment.EncData;
import org.graalvm.nativeimage.Isolate;
import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.Isolates;
import org.graalvm.nativeimage.StackValue;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.word.WordFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.apache.teaclave.javasdk.enclave.EnclaveTestHelper.MATH_ADD_PARAM_TYPES;
import static org.apache.teaclave.javasdk.enclave.EnclaveTestHelper.MATH_SERVICE;
import static org.apache.teaclave.javasdk.enclave.EnclaveTestHelper.NUMERIC_MATH;
import static org.apache.teaclave.javasdk.enclave.EnclaveTestHelper.isInNativeImage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

public class SVMSimpleEnclaveCallTest {
    private IsolateThread isolateThread;

    @BeforeEach
    public void setup() {
        if (isInNativeImage()) {
            isolateThread = Isolates.createIsolate(Isolates.CreateIsolateParameters.getDefault());
        }
    }

    @AfterEach
    public void teardown() {
        if (isInNativeImage()) {
            Isolates.tearDownIsolate(isolateThread);
        }
    }

    @Test
    public void test() throws IOException, ClassNotFoundException {
        if (!isInNativeImage()) {
            return;
        }
        ServiceHandler[] serviceHandlers = callLoadService(MATH_SERVICE);
        assertEquals(3, serviceHandlers.length);
        assertEquals(NUMERIC_MATH, serviceHandlers[0].getServiceImplClassName());
        String identity = serviceHandlers[0].getInstanceIdentity();
        EnclaveInvocationResult result = callEnclaveMethod(MATH_SERVICE,
                NUMERIC_MATH, identity,
                "add",
                MATH_ADD_PARAM_TYPES,
                new Object[]{1, 2});
        assertNotNull(result);
        Object wrappedResult = result.getResult();
        if (result.getException() != null) {
            result.getException().printStackTrace();
        }
        assertNotNull(wrappedResult, "Expect to have non-null result from invoking service method call.");
        assertNull(result.getException());
        assertEquals(3, (Integer) wrappedResult);
    }

    private ServiceHandler[] callLoadService(String serviceName) throws IOException, ClassNotFoundException {
        EnclaveInvocationResult ret = callEnclaveEntryPoint(serviceName,
                (isolateThread, input, result, callbacks) -> EnclaveEntry.loadService(isolateThread, input, result, callbacks));
        if (ret.getException() != null) {
            ret.getException().printStackTrace();
            fail();
        }
        return (ServiceHandler[]) ret.getResult();
    }

    private EnclaveInvocationResult callUnloadService(String serviceName) throws IOException, ClassNotFoundException {
        return callEnclaveEntryPoint(serviceName,
                (isolateThread, input, result, callbacks) -> EnclaveEntry.unloadService(isolateThread, input, result, callbacks));
    }

    private EnclaveInvocationResult callEnclaveMethod(String service, String impl, String identity, String method, String[] paramTypes, Object[] values) throws IOException, ClassNotFoundException {
        EnclaveInvocationContext enclaveInvocationContext = new EnclaveInvocationContext(new ServiceHandler(service, impl
                , identity), method, paramTypes, values);
        return callEnclaveEntryPoint(enclaveInvocationContext,
                (isolateThread, input, result, callbacks) -> EnclaveEntry.javaEnclaveInvoke(isolateThread, input, result, callbacks));
    }

    private EnclaveInvocationResult callEnclaveEntryPoint(Object input, Caller caller) throws IOException, ClassNotFoundException {
        byte[] data = SerializationHelper.serialize(input);
        EncData entryInput = StackValue.get(EncData.class);
        EncData entryResult = StackValue.get(EncData.class);
        try (CTypeConversion.CCharPointerHolder byteHolder = CTypeConversion.toCBytes(data)) {
            entryInput.setData(byteHolder.get());
            entryInput.setLen(data.length);
        }
        CallBacks callBacks = StackValue.get(CallBacks.class);
        callBacks.setExceptionHandler(WordFactory.nullPointer());
        callBacks.setMemCpyCCharPointerFunctionPointer(WordFactory.nullPointer());
        int ret = caller.call(Isolates.getIsolate(isolateThread), entryInput, entryResult, callBacks);
        if (ret == 0) {
            return (EnclaveInvocationResult) SerializationHelper.deserialize(transformInput(entryResult));
        } else {
            throw new RuntimeException("Fail to execute enclave method, return value is " + ret);
        }
    }

    @FunctionalInterface
    private interface Caller {
        int call(Isolate isolate, EncData input, EncData result, CallBacks callBacks);
    }

    private static byte[] transformInput(EncData input) {
        int len = input.getLen();
        byte[] data = new byte[len];
        CTypeConversion.asByteBuffer(input.getData(), len).get(data);
        return data;
    }
}
