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

/**
 * This class stores a method's necessary information for reflection
 * call by socket http, including the service instance's unique instanceIdentity,
 * interface name, class name, method name and its parameters.
 * It's used for embedded lib os inner service invocation.
 */
public final class SocketEnclaveInvocationContext extends EnclaveInvocationContext {
    private static final long serialVersionUID = 6202620980098144988L;
    public static final String SERVICE_LOADING = "service_loading";
    public static final String SERVICE_UNLOADING = "service_unloading";
    public static final String METHOD_INVOCATION = "method_invocation";
    public static final String REMOTE_ATTESTATION_GENERATE = "remote_attestation_generate";
    public static final String ENCLAVE_DESTROY = "enclave_destroy";

    private final String agentServiceName;
    private final byte[] userData;

    public SocketEnclaveInvocationContext(
            String agentServiceName,
            EnclaveInvocationContext context) {
        super(context.getServiceHandler(), context.getMethodName(), context.getParameterTypes(), context.getArguments());
        this.agentServiceName = agentServiceName;
        this.userData = null;
    }

    public SocketEnclaveInvocationContext(String agentServiceName, ServiceHandler serviceHandler) {
        super(serviceHandler);
        this.agentServiceName = agentServiceName;
        this.userData = null;
    }

    public SocketEnclaveInvocationContext(String agentServiceName,  byte[] userData) {
        this.agentServiceName = agentServiceName;
        this.userData = userData;
    }

    public SocketEnclaveInvocationContext(String agentServiceName) {
        this.agentServiceName = agentServiceName;
        this.userData = null;
    }

    public String getAgentServiceName() {
        return agentServiceName;
    }

    public byte[] getUserData() {
        return userData;
    }
}
