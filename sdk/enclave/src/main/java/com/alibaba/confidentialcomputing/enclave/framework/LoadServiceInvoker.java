package com.alibaba.confidentialcomputing.enclave.framework;

import com.alibaba.confidentialcomputing.common.EnclaveInvocationResult;

/**
 * This class handles loadService method invocation.
 */
public final class LoadServiceInvoker implements EnclaveMethodInvoker<String> {

    /**
     * Call loadService method.
     *
     * @param inputData name of the service to load.
     */
    @Override
    public EnclaveInvocationResult callMethod(String inputData) {
        Class<?> service;
        try {
            service = Class.forName(inputData);
            return new EnclaveInvocationResult(EnclaveContext.getInstance().loadService(service), null);
        } catch (ClassNotFoundException e) {
            return new EnclaveInvocationResult(null, e);
        }
    }
}
