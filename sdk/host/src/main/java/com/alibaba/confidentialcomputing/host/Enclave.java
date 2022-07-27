package com.alibaba.confidentialcomputing.host;

import java.util.Iterator;

import com.alibaba.confidentialcomputing.host.exception.ServicesLoadingException;
import com.alibaba.confidentialcomputing.host.exception.EnclaveDestroyingException;

/**
 * A {@code Enclave} is a TEE(Trust Execution Environment) instance.
 * It was created by EnclaveFactory class {@link EnclaveFactory},
 * Enclave provides a confidential computing environment to process
 * the work which it's very privacy and don't hope it was monitored
 * by any others, especially public cloud platform and os kernel.
 *
 * <pre>
 * +-------------------------------+  +-----------------------------+
 * |             Host              |  |            Enclave          |
 * |                               |  |                             |
 * |   EnclaveFactory.create() +----->|                             |
 * |                               |  |                             |
 * |     Enclave.load()   +-------------------> providers loaded    |
 * |                               |  |                             |
 * |     proxy.providers  <-------------------+                     |
 * |                               |  |                             |
 * |     proxy.invoker()  +-------------------> provider call       |
 * |                               |  |                             |
 * |          result      <-------------------+                     |
 * |        ... ... ...            |  |        ... ... ...          |
 * |        ... ... ...            |  |        ... ... ...          |
 * |    Enclave.destroy() +---------->|                             |
 * |                               |  |                             |
 * +-------------------------------+  +-----------------------------+
 * </pre>
 * <p>
 * The figure above describes an enclave's usual work flow.
 * <p>
 * In most cases, an enclave will be created first, then load services
 * from enclave, next you could invoke the service's method in the enclave.
 * the method's running middle-state data and its algorithm will be protected.
 * At last, don't forget to destroy the enclave instance.
 *
 * <pre>
 *    try {
 *        Enclave enclave = EnclaveFactory.create();
 *        AttestationReport report = RemoteAttestation.generateAttestationReport(enclave, new byte[64]);
 *        int valid = RemoteAttestation.verifyAttestationReport(report);
 *        if (valid == 0) {
 *            ... ... ...
 *        }
 *        ... ... ...
 *        Service provider = enclave.load(Service.class);
 *        ... ... ...
 *        Object result = provider.invoke();
 *        ... ... ...
 *        ... ... ...
 *        enclave.destroy();
 *    } catch(ConfidentialComputingException e) {
 *        // exception handle.
 *    }
 * </pre>
 */
public interface Enclave {
    /**
     * Returns all providers which implement service interface. It's similar to SPI
     * ServiceLoader mechanism. It returns proxy providers which are handlers to real
     * services loaded in enclave.
     * <p>
     *
     * @param <T>     Service interface type
     * @param service Must be a service interface
     * @return An iterator of providers were discovered.
     * @throws ServicesLoadingException {@link ServicesLoadingException} If proxy providers created
     *                                  failed or service handlers loaded failed in enclave.
     */
    <T> Iterator<T> load(Class<T> service) throws ServicesLoadingException;

    /**
     * Returns enclave's enclave info. Such as enclave's type, is it debuggable and so on.
     * <p>
     *
     * @return EnclaveInfo enclave information.
     */
    EnclaveInfo getEnclaveInfo();

    /**
     * This method destroy the enclave instance, all the resources in the enclave will be released.
     * <p>
     *
     * @throws EnclaveDestroyingException If underlying c/c++ enclave destroy failed.
     */
    void destroy() throws EnclaveDestroyingException;
}