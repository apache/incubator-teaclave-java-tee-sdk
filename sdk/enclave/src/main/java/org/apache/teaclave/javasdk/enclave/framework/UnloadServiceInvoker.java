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

package org.apache.teaclave.javasdk.enclave.framework;

import org.apache.teaclave.javasdk.common.EnclaveInvocationResult;
import org.apache.teaclave.javasdk.common.ServiceHandler;
import org.apache.teaclave.javasdk.common.exception.ConfidentialComputingException;

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
