package com.alibaba.confidentialcomputing.enclave.framework;


import com.alibaba.confidentialcomputing.common.EnclaveInvocationResult;
import com.alibaba.confidentialcomputing.common.ServiceHandler;
import com.alibaba.confidentialcomputing.common.exception.ConfidentialComputingException;

/**
 * This class handles the unloadService method to unload the specified service.
 */
public class UnloadServiceInvoker implements EnclaveMethodInvoker<ServiceHandler> {

    @Override
    public EnclaveInvocationResult callMethod(ServiceHandler inputData) {
        Object ret = EnclaveContext.getInstance().removeCache(inputData.getInstanceIdentity());
        Throwable t = null;
        if (ret == null) {
            t = new ConfidentialComputingException(String.format("No instance for service %s is found with the given identity %s", inputData.getServiceInterfaceName(),
                    inputData.getInstanceIdentity()));
        }
        // unloadService method's return type is void.
        return new EnclaveInvocationResult(null, t);
    }
}
