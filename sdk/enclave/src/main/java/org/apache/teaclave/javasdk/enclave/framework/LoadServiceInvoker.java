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

/**
 * This class handles loadService method invocation.
 */
public final class LoadServiceInvoker implements EnclaveMethodInvoker<String> {

    /**
     * Call loadService method.
     *
     * @param inputData name of the service to load.
     */
    @Override
    public EnclaveInvocationResult callMethod(String inputData) {
        try {
            Class<?> service = Class.forName(inputData);
            return new EnclaveInvocationResult(EnclaveContext.getInstance().loadService(service), null);
        } catch (ClassNotFoundException e) {
            return new EnclaveInvocationResult(null, e);
        }
    }
}
