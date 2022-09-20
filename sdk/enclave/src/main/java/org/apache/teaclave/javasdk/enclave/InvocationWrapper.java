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

import org.apache.teaclave.javasdk.common.EnclaveInvocationResult;
import org.apache.teaclave.javasdk.common.SerializationHelper;
import org.apache.teaclave.javasdk.common.exception.ConfidentialComputingException;
import org.apache.teaclave.javasdk.enclave.c.EnclaveEnvironment.CallBacks;
import org.apache.teaclave.javasdk.enclave.c.EnclaveEnvironment.EncData;
import org.apache.teaclave.javasdk.enclave.framework.EnclaveMethodInvoker;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.word.PointerBase;

import java.io.IOException;

/**
 * This class deals with the whole method invocation process from native entry point to the actual Java invocation target.
 * It is taken out in 3 steps:<p>
 * <li>Transform the input data from C {@link PointerBase} to Java byte[] and then deserialize to get the actual input.</li>
 * <li>Make the method invocation.</li>
 * <li>Collect the returned value, serialize it to the Java byte[], and wrap it back to C {@link PointerBase}</li>
 * </p>
 */
public class InvocationWrapper {

    public static <T> void invoke(EncData input, EncData result, CallBacks callBacks, EnclaveMethodInvoker<T> invoker) throws IOException {
        long start = System.nanoTime();
        byte[] data = transformInput(input);
        EnclaveInvocationResult ret;
        try {
            ret = invoker.callMethod((T) SerializationHelper.deserialize(data));
        } catch (Throwable t) {
            ret = new EnclaveInvocationResult(null, new ConfidentialComputingException(t));
        }
        // Set method returned value to result parameter
        ret.setCost(System.nanoTime() - start);
        wrapReturnValue(result, callBacks, ret);
    }

    private static void wrapReturnValue(EncData result, CallBacks callBacks, EnclaveInvocationResult ret) throws IOException {
        byte[] returnedValBytes;
        returnedValBytes = SerializationHelper.serialize(ret);
        int returnedValLen = returnedValBytes.length;
        /*
         * Data returned to C world should be allocated by the callback function in the C world. The memory hold by
         * returnedValBytes shall be freed in the explicit finally clause.
         */
        try (CTypeConversion.CCharPointerHolder byteHolder = CTypeConversion.toCBytes(returnedValBytes)) {
            CCharPointer returned;
            if (callBacks.isNonNull() && callBacks.getMemCpyCCharPointerFunctionPointer().isNonNull()) {
                returned = callBacks.getMemCpyCCharPointerFunctionPointer().invoke(byteHolder.get(), returnedValLen);
            } else {
                returned = byteHolder.get();
                System.out.println("Warning: Not calling call backs in native, there is memory leak risk.");
                //throw new RuntimeException("Function pointer memcpy_char_pointer is not set");
            }
            result.setData(returned);
            result.setLen(returnedValLen);
        }
    }

    /**
     * Transform input data from WordBase type to Java type.
     */
    private static byte[] transformInput(EncData input) {
        int len = input.getLen();
        byte[] data = new byte[len];
        CTypeConversion.asByteBuffer(input.getData(), len).get(data);
        return data;
    }
}
