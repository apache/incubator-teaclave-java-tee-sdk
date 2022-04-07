package com.alibaba.confidentialcomputing.enclave.framework;

import com.alibaba.confidentialcomputing.common.EnclaveInvocationResult;

/**
 * There are two types of method invocations in Enclave:
 * <ul>
 * <li>Business methods: The subclass {@link ServiceMethodInvoker} of this class takes care
 * of the business method invocation.</li>
 * <li>Framework methods: The SDK defined methods that run inside the enclave to maintain the framework. These methods
 * must be static, are taken care by the subclass {@link LoadServiceInvoker}.</li>
 * </ul>
 */
public interface EnclaveMethodInvoker<T> {
    EnclaveInvocationResult callMethod(T input);
}
