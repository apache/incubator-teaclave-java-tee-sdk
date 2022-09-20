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

import java.util.List;

/**
 * EnclaveInfoMXBean help query created all existed enclave's detail information.
 * Such as existed enclave instance number, every enclave's type info, is debuggable,
 * and enclave's epc memory size.
 */
public interface EnclaveInfoMXBean {
    /**
     * get the number of all existed enclaves.
     *
     * @return number of all existed enclaves.
     */
    int getEnclaveInstanceNumber();

    /**
     * get all existed enclaves' EnclaveInfo details.
     *
     * @return List<EnclaveInfo> all existed enclaves' EnclaveInfo details.
     */
    List<EnclaveInfo> getEnclaveInstancesInfo();
}
