package com.alibaba.confidentialcomputing.host;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import com.alibaba.confidentialcomputing.common.EnclaveInvocationResult;
import com.alibaba.confidentialcomputing.common.EnclaveInvocationContext;
import com.alibaba.confidentialcomputing.common.SerializationHelper;
import com.alibaba.confidentialcomputing.common.ServiceHandler;
import com.alibaba.confidentialcomputing.host.exception.EnclaveCreatingException;
import com.alibaba.confidentialcomputing.host.exception.EnclaveMethodInvokingException;
import com.alibaba.confidentialcomputing.host.exception.RemoteAttestationException;
import com.alibaba.confidentialcomputing.host.exception.ServicesLoadingException;
import com.alibaba.confidentialcomputing.host.exception.ServicesUnloadingException;


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
        enclaveContext = new EnclaveContext(type, mode, recycler);
    }

    AbstractEnclave(EnclaveType type, BaseEnclaveServicesRecycler recycler) {
        enclaveContext = new EnclaveContext(type, EnclaveDebug.NONE, recycler);
    }

    EnclaveContext getEnclaveContext() {
        return enclaveContext;
    }

    abstract InnerNativeInvocationResult loadServiceNative(byte[] payload);

    abstract InnerNativeInvocationResult unloadServiceNative(byte[] payload);

    abstract InnerNativeInvocationResult invokeMethodNative(byte[] payload);

    // load service by interface name.
    ServiceHandler[] loadService(Class<?> service) throws ServicesLoadingException {
        if (!getEnclaveContext().getEnclaveToken().tryAcquireToken()) {
            throw new ServicesLoadingException("enclave was destroyed.");
        }
        try {
            // Only need to provide service's interface name is enough to load service
            // in enclave.
            byte[] payload;
            try {
                payload = SerializationHelper.serialize(service.getName());
            } catch (IOException e) {
                throw new ServicesLoadingException("service name serialization failed.", e);
            }
            InnerNativeInvocationResult resultNativeWrapper = loadServiceNative(payload);
            // If loadServiceNative native call return value is error, an ServicesLoadingException exception
            // will be thrown.
            if (resultNativeWrapper.getRet() != 0) {
                throw new ServicesLoadingException("load service native call failed.");
            }
            EnclaveInvocationResult resultWrapper;
            try {
                resultWrapper = (EnclaveInvocationResult) SerializationHelper.deserialize(resultNativeWrapper.getPayload());
            } catch (IOException | ClassNotFoundException e) {
                throw new ServicesLoadingException("EnclaveInvokeResultWrapper deserialization failed.", e);
            }
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
            return (ServiceHandler[]) result;
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
        try {
            byte[] payload;
            try {
                payload = SerializationHelper.serialize(service);
            } catch (IOException e) {
                throw new ServicesUnloadingException("unload service serialization failed.", e);
            }
            InnerNativeInvocationResult resultNativeWrapper = unloadServiceNative(payload);
            if (resultNativeWrapper.getRet() != 0) {
                throw new ServicesUnloadingException("unload service native call failed.");
            }
            EnclaveInvocationResult resultWrapper;
            try {
                resultWrapper = (EnclaveInvocationResult) SerializationHelper.deserialize(resultNativeWrapper.getPayload());
            } catch (IOException | ClassNotFoundException e) {
                throw new ServicesUnloadingException("EnclaveInvokeResultWrapper deserialization failed.", e);
            }
            Throwable exception = resultWrapper.getException();
            if (exception != null) {
                throw new ServicesUnloadingException("service unload exception happened in enclave.", exception);
            }
        } finally {
            getEnclaveContext().getEnclaveToken().restoreToken();
        }
    }

    // it was called in service's proxy handler.
    Object InvokeEnclaveMethod(EnclaveInvocationContext input) throws EnclaveMethodInvokingException {
        if (!getEnclaveContext().getEnclaveToken().tryAcquireToken()) {
            throw new EnclaveMethodInvokingException("enclave was destroyed.");
        }
        try {
            byte[] payload;
            try {
                payload = SerializationHelper.serialize(input);
            } catch (IOException e) {
                throw new EnclaveMethodInvokingException("EnclaveInvokeMetaWrapper serialization failed.", e);
            }
            InnerNativeInvocationResult resultNativeWrapper = invokeMethodNative(payload);
            if (resultNativeWrapper.getRet() != 0) {
                throw new EnclaveMethodInvokingException("method invoke native call failed.");
            }
            EnclaveInvocationResult resultWrapper;
            try {
                resultWrapper = (EnclaveInvocationResult) SerializationHelper.deserialize(resultNativeWrapper.getPayload());
            } catch (IOException | ClassNotFoundException e) {
                throw new EnclaveMethodInvokingException("EnclaveInvokeResultWrapper deserialization failed.", e);
            }
            Throwable exception = resultWrapper.getException();
            if (exception != null) {
                EnclaveMethodInvokingException e = new EnclaveMethodInvokingException("method invoke exception happened in enclave.");
                e.initCause(exception);
                throw e;
            }
            return resultWrapper.getResult();
        } finally {
            getEnclaveContext().getEnclaveToken().restoreToken();
        }
    }

    abstract AttestationReport generateAttestationReport(byte[] userData) throws RemoteAttestationException;

    @Override
    public <T> Iterator<T> load(Class<T> service) throws ServicesLoadingException {
        // Check service must be an interface class.
        if (!service.isInterface()) {
            throw new ServicesLoadingException("service type: " + service.getTypeName() + " is not an interface type.");
        }

        // If enclave type is MOCK_IN_JVM, loading services by JDK SPI mechanism directly.
        if (enclaveContext.getEnclaveType() == EnclaveType.MOCK_IN_JVM) {
            ServiceLoader<T> loader = ServiceLoader.load(service);
            return loader.iterator();
        }

        // Loading services in enclave and creating proxy for them.
        Class<?>[] serviceInterface = new Class[1];
        serviceInterface[0] = service;

        List<T> serviceProxies = new ArrayList<T>();
        ServiceHandler[] services = loadService(service);
        for (ServiceHandler serviceHandler : services) {
            ProxyEnclaveInvocationHandler handler = new ProxyEnclaveInvocationHandler(this, serviceHandler);
            T proxy = (T) Proxy.newProxyInstance(service.getClassLoader(), serviceInterface, handler);
            serviceProxies.add(proxy);
            // Register proxy handler for enclave's corresponding service gc recycling.
            enclaveContext.getEnclaveServicesRecycler().registerProxyHandler(handler);
        }
        return serviceProxies.iterator();
    }

    /**
     * EnclaveContext cache an enclave's common information, such as
     * enclave type and debug mode, and each enclave instance has a service
     * resource recycle processor.
     */
    class EnclaveContext {
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
