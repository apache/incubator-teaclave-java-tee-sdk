package com.alibaba.confidentialcomputing.enclave.agent;

import com.alibaba.confidentialcomputing.common.EnclaveInvocationContext;
import com.alibaba.confidentialcomputing.common.EnclaveInvocationResult;
import com.alibaba.confidentialcomputing.common.SerializationHelper;
import com.alibaba.confidentialcomputing.common.ServiceHandler;
import com.alibaba.confidentialcomputing.common.EmbeddedLibOSInnerAttestationReport;
import com.alibaba.confidentialcomputing.common.exception.ConfidentialComputingException;
import com.alibaba.confidentialcomputing.enclave.framework.EnclaveMethodInvoker;
import com.alibaba.confidentialcomputing.enclave.framework.LoadServiceInvoker;
import com.alibaba.confidentialcomputing.enclave.framework.ServiceMethodInvoker;
import com.alibaba.confidentialcomputing.enclave.framework.UnloadServiceInvoker;

import java.io.IOException;

public class EnclaveAgentServiceImpl {
    private static final LoadServiceInvoker loadServiceInstance = new LoadServiceInvoker();
    private static final ServiceMethodInvoker serviceInvokerInstance = new ServiceMethodInvoker();
    private static final UnloadServiceInvoker unloadServiceInstance = new UnloadServiceInvoker();

    protected EnclaveAgentServiceImpl() {
    }

    private <T> byte[] invoke(EnclaveMethodInvoker<T> invoker, T input) {
        long start = System.nanoTime();
        EnclaveInvocationResult ret;
        try {
            ret = invoker.callMethod(input);
        } catch (Throwable t) {
            ret = new EnclaveInvocationResult(null, new ConfidentialComputingException(t));
        }
        ret.setCost(System.nanoTime() - start);
        try {
            return SerializationHelper.serialize(ret);
        } catch (IOException ex) {
        }
        return null;
    }

    public byte[] loadService(String serviceName) {
        return invoke(loadServiceInstance, serviceName);
    }

    public byte[] unloadService(ServiceHandler handler) {
        return invoke(unloadServiceInstance, handler);
    }

    public byte[] invokeMethod(EnclaveInvocationContext context) {
        return invoke(serviceInvokerInstance, context);
    }

    public byte[] generateAttestationReport(byte[] userDate) {
        EmbeddedLibOSInnerAttestationReport report = null;
        Throwable exception = null;
        try {
            report = RemoteAttestation.generateAttestationReport(userDate);
        } catch (ConfidentialComputingException e) {
            exception = e;
        }

        try {
            return SerializationHelper.serialize(new EnclaveInvocationResult(report, exception));
        } catch (IOException e) {
            try {
                return SerializationHelper.serialize(new EnclaveInvocationResult(null, e));
            } catch (IOException ex) {
            }
        }
        return null;
    }

    public byte[] destroy() {
        EnclaveShutDown.shutDownNotify();
        try {
            return SerializationHelper.serialize(new EnclaveInvocationResult(true, null));
        } catch (IOException e) {
        }
        return null;
    }
}
