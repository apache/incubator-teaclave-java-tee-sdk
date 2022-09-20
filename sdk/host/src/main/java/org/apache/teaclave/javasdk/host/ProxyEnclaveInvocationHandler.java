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

package org.apache.teaclave.javasdk.host;

import org.apache.teaclave.javasdk.common.EnclaveInvocationContext;
import org.apache.teaclave.javasdk.common.EnclaveInvocationResult;
import org.apache.teaclave.javasdk.common.ServiceHandler;
import org.apache.teaclave.javasdk.common.exception.ConfidentialComputingException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * ProxyEnclaveInvocationHandler define a service's proxy invocation handler.
 */
final class ProxyEnclaveInvocationHandler implements InvocationHandler, Runnable {
    private final AbstractEnclave enclave;
    private final ServiceHandler serviceHandler;

    ProxyEnclaveInvocationHandler(AbstractEnclave enclave, ServiceHandler serviceHandler) {
        this.enclave = enclave;
        this.serviceHandler = serviceHandler;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        EnclaveInvocationContext methodInvokeMetaWrapper;
        String[] parameterTypes;
        // Building a method wrapper for enclave native invocation.
        if (args != null) {
            parameterTypes = new String[args.length];
            // Get a method's parameter type exactly.
            Class<?>[] paraTypes = method.getParameterTypes();
            for (int index = 0x0; index < args.length; index++) {
                parameterTypes[index] = paraTypes[index].getName();
            }
            methodInvokeMetaWrapper = new EnclaveInvocationContext(
                    serviceHandler,
                    method.getName(),
                    parameterTypes,
                    args);
        } else {
            methodInvokeMetaWrapper = new EnclaveInvocationContext(
                    serviceHandler,
                    method.getName(), null, null);
        }

        // Handle service method invocation exception.
        try (MetricTraceContext trace = new MetricTraceContext(
                enclave.getEnclaveInfo(),
                MetricTraceContext.LogPrefix.METRIC_LOG_ENCLAVE_SERVICE_INVOKING_PATTERN,
                method.getName())) {
            EnclaveInvocationResult result = enclave.InvokeEnclaveMethod(methodInvokeMetaWrapper);
            trace.setCostInnerEnclave(result.getCost());
            Throwable causeException = result.getException();
            if (causeException instanceof ConfidentialComputingException) {
                Throwable enclaveCauseException = causeException.getCause();
                Class<?>[] exceptionTypes = method.getExceptionTypes();
                if (enclaveCauseException instanceof InvocationTargetException) {
                    // Check whether cause exception matches one of the method's exception declaration.
                    // If it's true, it illustrates that an exception happened in enclave when the service
                    // method was invoked in enclave, we should throw this exception directly and user will
                    // handle it.
                    // If it's false, it illustrates that an exception happened in host side or enclave side,
                    // but the exception is not belong to the method's declaration. In the case we should throw
                    // EnclaveMethodInvokingException again.
                    Throwable rootCause = enclaveCauseException.getCause();
                    for (Class<?> exception : exceptionTypes) {
                        if (exception == rootCause.getClass()) {
                            throw rootCause;
                        }
                    }
                }
            }
            return result.getResult();
        }
    }

    AbstractEnclave getEnclave() {
        return enclave;
    }

    ServiceHandler getServiceHandler() {
        return serviceHandler;
    }

    // If a proxy handler object was recycled by gc in host side, tell EnclaveServicesRecycle to
    // recycle the corresponding service loaded in enclave.
    @Override
    public void run() {
        enclave.getEnclaveContext().getEnclaveServicesRecycler().enqueueProxyHandler(this);
    }
}
