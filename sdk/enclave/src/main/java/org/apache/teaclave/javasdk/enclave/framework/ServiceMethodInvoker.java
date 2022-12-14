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

package org.apache.teaclave.javasdk.enclave.framework;

import org.apache.teaclave.javasdk.common.EnclaveInvocationContext;
import org.apache.teaclave.javasdk.common.EnclaveInvocationResult;
import org.apache.teaclave.javasdk.common.ServiceHandler;
import org.apache.teaclave.javasdk.common.exception.ConfidentialComputingException;
import jdk.vm.ci.meta.MetaUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class handles the service method invocation. The user defined business methods that run inside the enclave follow the
 * SPI (<a href="https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html">Service Provider Interface</a>)
 * idiom, so they are defined in the form of service methods. This class delegates the user method invocation request
 * wrapped in {@link EnclaveInvocationContext} to the actual method by reflection.
 */
public final class ServiceMethodInvoker implements EnclaveMethodInvoker<EnclaveInvocationContext> {

    /**
     * Prepare and make the target method call by reflection. Any exception thrown from method invocation is captured
     * and saved in the returned {@link EnclaveInvocationResult}. This method can only throw exception happens at invocation
     * preparation time.
     *
     * @param inputData all necessary information to reflectively invoke the target method.
     * @return value returned by the target method invocation or the exception captured in method invocation.
     */
    @Override
    public EnclaveInvocationResult callMethod(EnclaveInvocationContext inputData) {
        Throwable throwable = null;
        Object returnedValue = null;
        List<Class<?>> parameterClassList = extractParamClasses(inputData.getParameterTypes());
        ServiceHandler serviceHandler = inputData.getServiceHandler();
        String instanceIdentity = serviceHandler.getInstanceIdentity();
        String serviceName = serviceHandler.getServiceInterfaceName();
        String implementationClassName = serviceHandler.getServiceImplClassName();
        Object receiverInstance;
        try {
            receiverInstance = EnclaveContext.getInstance().lookupServiceInstance(instanceIdentity, serviceName, implementationClassName);
        } catch (ConfidentialComputingException e) {
            return new EnclaveInvocationResult(null, e);
        }
        if (receiverInstance != null) {
            String methodName = inputData.getMethodName();
            Method method;
            // Get the public method to invoke
            try {
                Class<?> serviceClass = Class.forName(implementationClassName);
                method = serviceClass.getMethod(methodName, parameterClassList.toArray(new Class<?>[0]));
                method.setAccessible(true);
            } catch (ReflectiveOperationException e) {
                // Reflection exception is taken as framework's exception
                return new EnclaveInvocationResult(null, new ConfidentialComputingException(e));
            }
            try {
                // Call the actual method
                returnedValue = method.invoke(receiverInstance, inputData.getArguments());
            } catch (Throwable t) {
                return new EnclaveInvocationResult(null, new ConfidentialComputingException(t));
            }
        } else {
            throwable = new ConfidentialComputingException(
                    String.format("Didn't match any service implementation with the given class name: %s", implementationClassName));
        }
        return new EnclaveInvocationResult(returnedValue, throwable);
    }

    private static List<Class<?>> extractParamClasses(String[] parameterTypes) {
        if (parameterTypes == null)  return Collections.emptyList();
        List<Class<?>> parameterClassList = new ArrayList<>();
        for (String parameterType : parameterTypes) {
            try {
                parameterClassList.add(nameToType(parameterType));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Can't found the specified class from parameters:", e);
            }
        }
        return parameterClassList;
    }

    private static Class<?> nameToType(String typeName) throws ClassNotFoundException {
        String name = typeName;
        if (name.indexOf('[') != -1) {
            /* accept "int[][]", "java.lang.String[]" */
            name = MetaUtil.internalNameToJava(MetaUtil.toInternalName(name), true, true);
        }
        if (name.indexOf('.') == -1) {
            switch (name) {
                case "boolean":
                    return boolean.class;
                case "char":
                    return char.class;
                case "float":
                    return float.class;
                case "double":
                    return double.class;
                case "byte":
                    return byte.class;
                case "short":
                    return short.class;
                case "int":
                    return int.class;
                case "long":
                    return long.class;
                case "void":
                    return void.class;
            }
        }
        return Class.forName(name);
    }
}
