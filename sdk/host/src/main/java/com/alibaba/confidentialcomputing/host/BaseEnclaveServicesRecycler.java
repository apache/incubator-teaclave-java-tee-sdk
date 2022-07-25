package com.alibaba.confidentialcomputing.host;

/**
 * BaseEnclaveServicesRecycler an empty enclave services recycler for MOCK_IN_JVM enclave.
 */
class BaseEnclaveServicesRecycler {
    BaseEnclaveServicesRecycler() {
    }

    void enqueueProxyHandler(ProxyEnclaveInvocationHandler handler) {
    }

    void registerProxyHandler(Object obj, ProxyEnclaveInvocationHandler handler) {
    }

    void interruptServiceRecycler() {
    }
}
