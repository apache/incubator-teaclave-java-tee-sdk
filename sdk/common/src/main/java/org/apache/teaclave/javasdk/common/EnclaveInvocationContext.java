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

package org.apache.teaclave.javasdk.common;

import java.io.Serializable;

/**
 * This class stores a method's necessary information for reflection
 * call, including the service instance's unique instanceIdentity, interface name, class name,
 * method name and its parameters.
 */
public class EnclaveInvocationContext implements Serializable {
    private static final long serialVersionUID = 6878585714134748604L;

    private final ServiceHandler serviceHandler;
    private final String methodName;
    private final String[] parameterTypes;
    private final Object[] arguments;

    public EnclaveInvocationContext() {
        this.serviceHandler = null;
        this.methodName = null;
        this.parameterTypes = null;
        this.arguments = null;
    }

    public EnclaveInvocationContext(ServiceHandler serviceHandler,
                                    String methodName,
                                    String[] parameterTypes,
                                    Object[] arguments) {
        this.serviceHandler = serviceHandler;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.arguments = arguments;
    }

    public EnclaveInvocationContext(ServiceHandler serviceHandler) {
        this.methodName = null;
        this.parameterTypes = null;
        this.arguments = null;
        this.serviceHandler = serviceHandler;
    }

    /**
     * get service handler.
     *
     * @return service handler.
     */
    public ServiceHandler getServiceHandler() {
        return serviceHandler;
    }

    /**
     * get the method's name.
     *
     * @return method's name.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * get all parameters' type.
     *
     * @return parameters' type information.
     */
    public String[] getParameterTypes() {
        return parameterTypes;
    }

    /**
     * get all arguments' value.
     *
     * @return arguments' value information.
     */
    public Object[] getArguments() {
        return arguments;
    }
}
