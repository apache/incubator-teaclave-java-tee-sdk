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

package org.apache.teaclave.javasdk.host;

/**
 * an enclave's information details.
 */
public interface EnclaveInfo {
    /**
     * the enclave's type, MOCK_IN_JVM、MOCK_IN_SVM、TEE_SDK or EMBEDDED_LIB_OS.
     */
    EnclaveType getEnclaveType();

    /**
     * the enclave's Hash ID.
     */
    int getEnclaveID();

    /**
     * is the enclave debuggable or not. MOCK_IN_JVM and MOCK_IN_SVM are simulation mode,
     * so these two mock enclave type are debuggable. TEE_SDK and EMBEDDED_LIB_OS depend on
     * user, if the enclave is not debuggable, the code and data in enclave is not accessible
     * by gdb or other debugging tools.
     */
    boolean isEnclaveDebuggable();

    /**
     * get enclave's usable epc memory size.
     */
    long getEnclaveEPCMemorySizeBytes();

    /**
     * get enclave's usable max threads number.
     */
    int getEnclaveMaxThreadsNumber();
}
