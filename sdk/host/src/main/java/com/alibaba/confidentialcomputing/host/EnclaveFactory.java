package com.alibaba.confidentialcomputing.host;

import com.alibaba.confidentialcomputing.host.exception.EnclaveCreatingException;
import com.alibaba.confidentialcomputing.host.exception.MetricTraceLogWriteException;

import java.io.IOException;

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
        // create an enclave without specific enclave type.
        // if -Dcom.alibaba.enclave.type is not set, TEE_SDK
        // type enclave will be created.
        try {
            return create(EnclaveConfigure.getInstance().getDefaultEnclaveType());
        } catch (IOException e) {
            throw new EnclaveCreatingException(e);
        }
    }

    /**
     * @param type explicitly indicate which type of enclave will be created.
     * @return An enclave instance.
     * @throws EnclaveCreatingException {@link EnclaveCreatingException} If underlying c/c++ enclave
     *                                  create failed.
     */
    public static Enclave create(EnclaveType type) throws EnclaveCreatingException {
        // create an enclave with specific enclave type.
        try (MetricTraceContext trace = new MetricTraceContext(MetricTraceContext.LogPrefix.METRIC_LOG_ENCLAVE_CREATING_PATTERN)) {
            Enclave enclave;
            switch (type) {
                case MOCK_IN_JVM:
                    enclave = new MockInJvmEnclave();
                    break;
                case MOCK_IN_SVM:
                    enclave = new MockInSvmEnclave();
                    break;
                case TEE_SDK:
                    // TEE_SDK only support hardware mode, not support simulate mode.
                    if (EnclaveConfigure.getInstance().isEnclaveDebuggable()) {
                        enclave = new TeeSdkEnclave(EnclaveDebug.DEBUG);
                    } else {
                        enclave = new TeeSdkEnclave(EnclaveDebug.RELEASE);
                    }
                    break;
                case EMBEDDED_LIB_OS:
                    // EMBEDDED_LIB_OS only support hardware mode, not support simulate mode.
                    if (EmbeddedLibOSEnclaveConfigure.getInstance().isEnclaveDebuggable()) {
                        enclave = EmbeddedLibOSEnclave.getEmbeddedLibOSEnclaveInstance(EnclaveDebug.DEBUG, EnclaveSimulate.HARDWARE);
                    } else {
                        enclave = EmbeddedLibOSEnclave.getEmbeddedLibOSEnclaveInstance(EnclaveDebug.RELEASE, EnclaveSimulate.HARDWARE);
                    }
                    break;
                case NONE:
                default:
                    throw new EnclaveCreatingException("enclave type: " + type + " is not supported.");
            }
            trace.setEnclaveInfo(enclave.getEnclaveInfo());
            EnclaveInfoManager.getEnclaveInfoManagerInstance().addEnclave(enclave);
            return enclave;
        } catch (IOException | MetricTraceLogWriteException e) {
            throw new EnclaveCreatingException(e);
        }
    }
}
