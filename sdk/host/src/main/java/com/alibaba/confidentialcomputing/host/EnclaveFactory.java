package com.alibaba.confidentialcomputing.host;

import com.alibaba.confidentialcomputing.host.exception.EnclaveCreatingException;

/**
 * Factory class for {@link Enclave}.
 * <p>
 * TEE is an abstract concept, it contains many kinds of confidential compute technology.
 * From hardware's point, there are Intel's SGX/TDX, Arm's TrustZone and so on.
 * From software's point, there are SGX-SDK, OpenEnclave, TeeSDK and so on.
 * JavaEnclave is committed to make java enclave development easy and efficient.
 * <p>
 * Java developer don't need to care too much about enclave's underlying technology stack.
 * And JavaEnclave will help java programmer develop a java enclave service as the same as
 * a common java service.
 * <pre>
 * try {
 *     Enclave enclave = EnclaveFactory.create();
 *     ... ... ...
 *     ... ... ...
 *     ... ... ...
 * } catch (EnclaveCreatingException e) {
 *     // exception handle.
 * }
 * </pre>
 */
public final class EnclaveFactory {
    /**
     * TeeSDK type enclave will be created by default.
     *
     * @return An enclave instance.
     * @throws EnclaveCreatingException {@link EnclaveCreatingException} If underlying c/c++ enclave
     *                                  create failed.
     */
    public static Enclave create() throws EnclaveCreatingException {
        return EnclaveConfigure.create();
    }

    /**
     * @param type explicitly indicate which type of enclave will be created.
     * @return An enclave instance.
     * @throws EnclaveCreatingException {@link EnclaveCreatingException} If underlying c/c++ enclave
     *                                  create failed.
     */
    public static Enclave create(EnclaveType type) throws EnclaveCreatingException {
        return EnclaveConfigure.create(type);
    }
}
