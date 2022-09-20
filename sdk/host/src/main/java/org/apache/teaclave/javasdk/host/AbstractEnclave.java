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

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.teaclave.javasdk.common.EnclaveInvocationResult;
import org.apache.teaclave.javasdk.common.EnclaveInvocationContext;
import org.apache.teaclave.javasdk.common.SerializationHelper;
import org.apache.teaclave.javasdk.common.ServiceHandler;
import org.apache.teaclave.javasdk.host.exception.*;

/**
 * AbstractEnclave implements all kinds of enclave platform's common operation.
 * Such as service loading、unloading and service method invocation.
 * But the underlying native implements is very different between these enclave platforms,
 * so enclave will implement these native calls in specific enclave platform.
 */
abstract class AbstractEnclave implements Enclave {
    private final EnclaveContext enclaveContext;

    AbstractEnclave(EnclaveType type,
                    EnclaveDebug mode,
                    BaseEnclaveServicesRecycler recycler) throws EnclaveCreatingException {
        if (type == EnclaveType.TEE_SDK && mode == EnclaveDebug.NONE) {
            throw new EnclaveCreatingException("TEE SDK enclave's debug mode must be RELEASE or DEBUG.");
        }
        if (type == EnclaveType.EMBEDDED_LIB_OS && mode == EnclaveDebug.NONE) {
            throw new EnclaveCreatingException("EMBEDDED_LIB_OS enclave's debug mode must be RELEASE or DEBUG.");
        }
        enclaveContext = new EnclaveContext(type, mode, recycler);
    }

    AbstractEnclave(EnclaveType type, BaseEnclaveServicesRecycler recycler) {
        enclaveContext = new EnclaveContext(type, EnclaveDebug.NONE, recycler);
    }

    EnclaveContext getEnclaveContext() {
        return enclaveContext;
    }

    abstract byte[] loadServiceNative(String service) throws ServicesLoadingException;

    abstract byte[] unloadServiceNative(ServiceHandler handler) throws ServicesUnloadingException;

    abstract byte[] invokeMethodNative(EnclaveInvocationContext context) throws EnclaveMethodInvokingException;

    abstract AttestationReport generateAttestationReportNative(byte[] userData) throws RemoteAttestationException;

    // load service by interface name in mock_jvm mode.
    // because mock_svm/tee_sdk/lib_os adopt serialization and deserialization between host and enclave,
    // while mock_jvm will call enclave service directly.
    <T> Iterator<T> loadProxyServiceMockJVM(Class<?> service) throws ServicesLoadingException {
        try (MetricTraceContext trace = new MetricTraceContext(
                this.getEnclaveInfo(),
                MetricTraceContext.LogPrefix.METRIC_LOG_ENCLAVE_SERVICE_LOADING_PATTERN,
                service.getName())) {
            List<T> serviceProxies = new ArrayList<>();
            Class<?>[] serviceInterface = new Class[]{service};

            ServiceLoader<T> innerProxyServices = (ServiceLoader<T>) ServiceLoader.load(service);
            for (T innerProxyService : innerProxyServices) {
                ProxyMockJvmInvocationHandler handler = new ProxyMockJvmInvocationHandler(this, innerProxyService);
                T proxy = (T) Proxy.newProxyInstance(service.getClassLoader(), serviceInterface, handler);
                serviceProxies.add(proxy);
                // Register proxy handler for enclave's corresponding service gc recycling.
                enclaveContext.getEnclaveServicesRecycler().registerProxyHandler(proxy, handler);
            }
            return serviceProxies.iterator();
        } catch (MetricTraceLogWriteException e) {
            throw new ServicesLoadingException(e);
        }
    }

    // load service by interface name in mock_svm/tee_sdk/lib_os enclave mode.
    <T> Iterator<T> loadProxyService(Class<?> service) throws ServicesLoadingException {
        if (!getEnclaveContext().getEnclaveToken().tryAcquireToken()) {
            throw new ServicesLoadingException("enclave was destroyed.");
        }
        try (MetricTraceContext trace = new MetricTraceContext(
                this.getEnclaveInfo(),
                MetricTraceContext.LogPrefix.METRIC_LOG_ENCLAVE_SERVICE_LOADING_PATTERN,
                service.getName())) {
            List<T> serviceProxies = new ArrayList<>();
            Class<?>[] serviceInterface = new Class[]{service};

            // Only need to provide service's interface name is enough to load service
            // in enclave.
            EnclaveInvocationResult resultWrapper;
            resultWrapper = (EnclaveInvocationResult) SerializationHelper.deserialize(loadServiceNative(service.getName()));
            trace.setCostInnerEnclave(resultWrapper.getCost());
            Throwable exception = resultWrapper.getException();
            Object result = resultWrapper.getResult();
            // this exception is transformed from enclave, so throw it and handle it in proxy handler.
            if (exception != null) {
                throw new ServicesLoadingException("service load exception happened in enclave.", exception);
            }
            // result should never be null, at least it should have an empty ServiceMirror type array.
            if (result == null) {
                throw new ServicesLoadingException("service load with no any result.");
            }
            if (!(result instanceof ServiceHandler[])) {
                throw new ServicesLoadingException("service load return type is not ServiceHandler[].");
            }

            for (ServiceHandler serviceHandler : (ServiceHandler[]) result) {
                ProxyEnclaveInvocationHandler handler = new ProxyEnclaveInvocationHandler(this, serviceHandler);
                T proxy = (T) Proxy.newProxyInstance(service.getClassLoader(), serviceInterface, handler);
                serviceProxies.add(proxy);
                // Register proxy handler for enclave's corresponding service gc recycling.
                enclaveContext.getEnclaveServicesRecycler().registerProxyHandler(proxy, handler);
            }
            return serviceProxies.iterator();
        } catch (IOException | ClassNotFoundException e) {
            throw new ServicesLoadingException("EnclaveInvokeResultWrapper deserialization failed.", e);
        } catch (MetricTraceLogWriteException e) {
            throw new ServicesLoadingException(e);
        } finally {
            getEnclaveContext().getEnclaveToken().restoreToken();
        }
    }

    // unload service by interface name、class name and service's unique identity in enclave.
    // it was called if the service handler was recycled by gc in host side.
    void unloadService(ServiceHandler service) throws ServicesUnloadingException {
        if (!getEnclaveContext().getEnclaveToken().tryAcquireToken()) {
            throw new ServicesUnloadingException("enclave was destroyed.");
        }
        try (MetricTraceContext trace = new MetricTraceContext(
                this.getEnclaveInfo(),
                MetricTraceContext.LogPrefix.METRIC_LOG_ENCLAVE_SERVICE_UNLOADING_PATTERN,
                service.getServiceImplClassName())) {
            EnclaveInvocationResult resultWrapper;
            resultWrapper = (EnclaveInvocationResult) SerializationHelper.deserialize(unloadServiceNative(service));
            trace.setCostInnerEnclave(resultWrapper.getCost());
            Throwable exception = resultWrapper.getException();
            if (exception != null) {
                throw new ServicesUnloadingException("service unload exception happened in enclave.", exception);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new ServicesUnloadingException("EnclaveInvokeResultWrapper deserialization failed.", e);
        } catch (MetricTraceLogWriteException e) {
            throw new ServicesUnloadingException(e);
        } finally {
            getEnclaveContext().getEnclaveToken().restoreToken();
        }
    }

    // it was called in service's proxy handler.
    EnclaveInvocationResult InvokeEnclaveMethod(EnclaveInvocationContext input) throws EnclaveMethodInvokingException {
        if (!getEnclaveContext().getEnclaveToken().tryAcquireToken()) {
            throw new EnclaveMethodInvokingException("enclave was destroyed.");
        }
        try {
            EnclaveInvocationResult resultWrapper;
            resultWrapper = (EnclaveInvocationResult) SerializationHelper.deserialize(invokeMethodNative(input));
            return resultWrapper;
        } catch (IOException | ClassNotFoundException e) {
            throw new EnclaveMethodInvokingException("EnclaveInvokeResultWrapper deserialization failed.", e);
        } finally {
            getEnclaveContext().getEnclaveToken().restoreToken();
        }
    }

    AttestationReport generateAttestationReport(byte[] userData) throws RemoteAttestationException {
        if (!getEnclaveContext().getEnclaveToken().tryAcquireToken()) {
            throw new RemoteAttestationException("enclave was destroyed.");
        }
        try {
            return generateAttestationReportNative(userData);
        } finally {
            getEnclaveContext().getEnclaveToken().restoreToken();
        }
    }

    @Override
    public <T> Iterator<T> load(Class<T> service) throws ServicesLoadingException {
        // Check service must be an interface class.
        if (!service.isInterface()) {
            throw new ServicesLoadingException("service type: " + service.getTypeName() + " is not an interface type.");
        }

        Iterator<T> serviceProxies;
        switch (enclaveContext.getEnclaveType()) {
            // If enclave type is MOCK_IN_JVM, loading services by JDK SPI mechanism directly.
            case MOCK_IN_JVM:
                serviceProxies = loadProxyServiceMockJVM(service);
                break;
            // Loading services in enclave and creating proxy for them.
            case MOCK_IN_SVM:
            case TEE_SDK:
            case EMBEDDED_LIB_OS:
            default:
                serviceProxies = loadProxyService(service);
        }
        return serviceProxies;
    }

    /**
     * EnclaveContext cache an enclave's common information, such as
     * enclave type and debug mode, and each enclave instance has a service
     * resource recycle processor.
     */
    static class EnclaveContext {
        // enclave's type.
        private final EnclaveType type;
        // enclave's debug mode.
        private final EnclaveDebug mode;
        // every enclave has a services' recycler.
        private final BaseEnclaveServicesRecycler enclaveServicesRecycler;
        // every enclave has an enclave token.
        private final EnclaveToken enclaveToken;

        EnclaveContext(EnclaveType type,
                       EnclaveDebug mode,
                       BaseEnclaveServicesRecycler recycler) {
            this.type = type;
            this.mode = mode;
            this.enclaveServicesRecycler = recycler;
            this.enclaveToken = new EnclaveToken();
        }

        EnclaveType getEnclaveType() {
            return type;
        }

        EnclaveDebug getEnclaveDebugMode() {
            return mode;
        }

        BaseEnclaveServicesRecycler getEnclaveServicesRecycler() {
            return enclaveServicesRecycler;
        }

        EnclaveToken getEnclaveToken() {
            return enclaveToken;
        }
    }
}
