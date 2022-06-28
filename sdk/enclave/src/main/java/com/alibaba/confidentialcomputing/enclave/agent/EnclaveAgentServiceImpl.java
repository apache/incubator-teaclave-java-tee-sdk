package com.alibaba.confidentialcomputing.enclave.agent;

import com.alibaba.confidentialcomputing.common.EnclaveInvocationContext;
import com.alibaba.confidentialcomputing.common.EnclaveInvocationResult;
import com.alibaba.confidentialcomputing.common.SerializationHelper;
import com.alibaba.confidentialcomputing.common.ServiceHandler;
import com.alibaba.confidentialcomputing.common.EmbeddedLibOSInnerAttestationReport;
import com.alibaba.confidentialcomputing.common.exception.ConfidentialComputingException;
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

    public byte[] loadService(String serviceName) {
        try {
            return SerializationHelper.serialize(loadServiceInstance.callMethod(serviceName));
        } catch (IOException e) {
            try {
                return SerializationHelper.serialize(new EnclaveInvocationResult(null, e));
            } catch (IOException ex) {
            }
        }
        return null;
    }

    public byte[] unloadService(ServiceHandler handler) {
        try {
            return SerializationHelper.serialize(unloadServiceInstance.callMethod(handler));
        } catch (IOException e) {
            try {
                return SerializationHelper.serialize(new EnclaveInvocationResult(null, e));
            } catch (IOException ex) {
            }
        }
        return null;
    }

    public byte[] invokeMethod(EnclaveInvocationContext context) {
        try {
            return SerializationHelper.serialize(serviceInvokerInstance.callMethod(context));
        } catch (IOException e) {
            try {
                return SerializationHelper.serialize(new EnclaveInvocationResult(null, e));
            } catch (IOException ex) {
            }
        }
        return null;
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
