package com.alibaba.confidentialcomputing.common;

import java.io.Serializable;

/**
 * EnclaveInvocationResult is a service's method invoking return value in an enclave.
 * If an exception happened during the invocation, the exception will be stored in the
 * EnclaveInvocationResult object's exception field, the result field will be null.
 * If no exception happened during the invocation, the method invoking return value is
 * stored in the EnclaveInvocationResult object's result field, and the exception field
 * will be null.
 */
public final class EnclaveInvocationResult implements Serializable {
    private static final long serialVersionUID = -571664787738930979L;

    private final Object resultedValue;
    private final Throwable exception;

    public EnclaveInvocationResult(Object result, Throwable exception) {
        this.resultedValue = result;
        this.exception = exception;
    }

    /**
     * get method's return value.
     *
     * @return method's return value.
     */
    public Object getResult() {
        return this.resultedValue;
    }

    /**
     * get exception during method's invocation.
     *
     * @return exception during method's invocation if it has.
     */
    public Throwable getException() {
        return this.exception;
    }
}