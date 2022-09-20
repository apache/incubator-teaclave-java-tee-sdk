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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * ProxyMockJvmInvocationHandler define a service's proxy invocation handler.
 * It mainly helps to metric trace the cost of a service invocation for
 * MOCK_IN_JVM enclave.
 */
final class ProxyMockJvmInvocationHandler<T> implements InvocationHandler, Runnable {
    private final AbstractEnclave enclave;
    private final T proxyService;

    ProxyMockJvmInvocationHandler(AbstractEnclave enclave, T proxyService) {
        this.enclave = enclave;
        this.proxyService = proxyService;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        try (MetricTraceContext trace = new MetricTraceContext(
                enclave.getEnclaveInfo(),
                MetricTraceContext.LogPrefix.METRIC_LOG_ENCLAVE_SERVICE_INVOKING_PATTERN,
                method.getName())) {
            result = method.invoke(proxyService, args);
        } catch (InvocationTargetException e) {
            // Check whether cause exception matches one of the method's exception declaration.
            // If it's true, it illustrates that an exception happened in enclave when the service
            // method was invoked in enclave, we should throw this exception directly and user will
            // handle it.
            // If it's false, it illustrates that an exception happened in host side or enclave side,
            // but the exception is not belong to the method's declaration. In the case we should throw
            // EnclaveMethodInvokingException again.
            Class<?>[] exceptionTypes = method.getExceptionTypes();
            Throwable rootCause = e.getCause();
            for (Class<?> exception : exceptionTypes) {
                if (exception == rootCause.getClass()) {
                    throw rootCause;
                }
            }
            throw e;
        }
        return result;
    }

    @Override
    public void run() {
        enclave.getEnclaveContext().getEnclaveServicesRecycler().enqueueProxyHandler(this);
    }
}
