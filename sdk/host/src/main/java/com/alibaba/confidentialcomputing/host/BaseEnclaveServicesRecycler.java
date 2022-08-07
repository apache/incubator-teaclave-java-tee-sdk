package com.alibaba.confidentialcomputing.host;

import java.lang.reflect.InvocationHandler;

/**
 * BaseEnclaveServicesRecycler an empty enclave services recycler for MOCK_IN_JVM enclave.
 */
class BaseEnclaveServicesRecycler {
    BaseEnclaveServicesRecycler() {
    }

    void enqueueProxyHandler(InvocationHandler handler) {
    }

    void registerProxyHandler(Object obj, InvocationHandler handler) {
    }

    void interruptServiceRecycler() {
    }
}
