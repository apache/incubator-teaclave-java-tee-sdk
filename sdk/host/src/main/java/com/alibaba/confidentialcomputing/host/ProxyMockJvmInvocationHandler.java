package com.alibaba.confidentialcomputing.host;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ProxyMockJvmInvocationHandler<T> implements InvocationHandler, Runnable {
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
