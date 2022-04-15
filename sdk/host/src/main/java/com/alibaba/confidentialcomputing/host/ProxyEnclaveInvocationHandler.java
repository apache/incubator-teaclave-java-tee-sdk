package com.alibaba.confidentialcomputing.host;

import com.alibaba.confidentialcomputing.common.EnclaveInvocationContext;
import com.alibaba.confidentialcomputing.common.ServiceHandler;
import com.alibaba.confidentialcomputing.common.exception.ConfidentialComputingException;
import com.alibaba.confidentialcomputing.host.exception.EnclaveMethodInvokingException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * ProxyEnclaveInvocationHandler define a service's proxy invocation handler.
 */
class ProxyEnclaveInvocationHandler implements InvocationHandler, Runnable {
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
        Object result;
        try {
            result = enclave.InvokeEnclaveMethod(methodInvokeMetaWrapper);
        } catch (EnclaveMethodInvokingException e) {
            // Get cause exception if it has one.
            ConfidentialComputingException enclaveException = (ConfidentialComputingException) e.getCause();
            Throwable enclaveCauseException = enclaveException.getCause();
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
            throw e;
        }
        return result;
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
