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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * EnclaveInfoManager stores all existed enclave's detail information. Such as the number of
 * all existed enclave instance, every enclave's type info, debuggable or not, and enclave's
 * epc memory size.
 */
public final class EnclaveInfoManager implements EnclaveInfoMXBean {
    private final static EnclaveInfoManager instance = new EnclaveInfoManager();
    private final HashMap<Enclave, Object> enclaveRecord = new HashMap<>();

    /**
     * get a single instance of EnclaveInfoManager.
     *
     * @return a single instance of EnclaveInfoManager.
     */
    public static EnclaveInfoManager getEnclaveInfoManagerInstance() {
        return instance;
    }

    synchronized void addEnclave(Enclave enclave) {
        enclaveRecord.put(enclave, null);
    }

    synchronized void removeEnclave(Enclave enclave) {
        enclaveRecord.remove(enclave);
    }

    /**
     * number of all existed enclaves.
     *
     * @return number of existed enclaves.
     */
    @Override
    public synchronized int getEnclaveInstanceNumber() {
        return enclaveRecord.size();
    }

    /**
     * get all existed enclaves' EnclaveInfo details.
     *
     * @return List<EnclaveInfo> all existed enclaves' EnclaveInfo details.
     */
    @Override
    public synchronized List<EnclaveInfo> getEnclaveInstancesInfo() {
        List<EnclaveInfo> enclaveInfos = new ArrayList<>();
        for (Enclave enclave : enclaveRecord.keySet()) {
            enclaveInfos.add(enclave.getEnclaveInfo());
        }
        return enclaveInfos;
    }
}
