package com.alibaba.confidentialcomputing.host;

/**
 * InnerNativeInvocationResult is load_service unload_service and invoke_method
 * native call's return value. It not only contains enclave e_call's return value,
 * also contains an EnclaveInvocationResult object's serialization payload from
 * method invocation in enclave.
 */
class InnerNativeInvocationResult {
    // enclave method native call's result.
    private final int ret;
    // payload is an EnclaveInvocationResult object's serialization data.
    private final byte[] payload;

    InnerNativeInvocationResult(int ret, byte[] payload) {
        this.ret = ret;
        this.payload = payload;
    }

    int getRet() {
        return ret;
    }

    byte[] getPayload() {
        return payload;
    }
}
