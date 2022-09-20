// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.teaclave.javasdk.enclave.agent;

import org.apache.teaclave.javasdk.common.EnclaveInvocationContext;
import org.apache.teaclave.javasdk.common.EnclaveInvocationResult;
import org.apache.teaclave.javasdk.common.SerializationHelper;
import org.apache.teaclave.javasdk.common.ServiceHandler;
import org.apache.teaclave.javasdk.common.EmbeddedLibOSInnerAttestationReport;
import org.apache.teaclave.javasdk.common.exception.ConfidentialComputingException;
import org.apache.teaclave.javasdk.enclave.framework.EnclaveMethodInvoker;
import org.apache.teaclave.javasdk.enclave.framework.LoadServiceInvoker;
import org.apache.teaclave.javasdk.enclave.framework.ServiceMethodInvoker;
import org.apache.teaclave.javasdk.enclave.framework.UnloadServiceInvoker;

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
        } catch (IOException ignored) {
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
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    public byte[] destroy() {
        EnclaveShutDown.shutDownNotify();
        try {
            return SerializationHelper.serialize(new EnclaveInvocationResult(true, null));
        } catch (IOException ignored) {
        }
        return null;
    }
}
