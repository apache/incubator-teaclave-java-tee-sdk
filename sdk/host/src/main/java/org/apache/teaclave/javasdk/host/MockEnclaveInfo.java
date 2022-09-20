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

import javax.management.ConstructorParameters;

final class MockEnclaveInfo implements EnclaveInfo {
    private final EnclaveType enclaveType;
    private final boolean isEnclaveDebuggable;
    private final long enclaveEPCMemorySizeBytes; // Bytes.
    private final int enclaveMaxThreadsNumber;
    private final int enclaveID;

    @ConstructorParameters({"enclaveType", "isEnclaveDebuggable", "enclaveEPCMemorySizeBytes", "enclaveMaxThreadsNumber"})
    MockEnclaveInfo(EnclaveType enclaveType, boolean isEnclaveDebuggable, long enclaveEPCMemorySizeBytes, int enclaveMaxThreadsNumber) {
        this.enclaveType = enclaveType;
        this.isEnclaveDebuggable = isEnclaveDebuggable;
        this.enclaveEPCMemorySizeBytes = enclaveEPCMemorySizeBytes;
        this.enclaveMaxThreadsNumber = enclaveMaxThreadsNumber;
        this.enclaveID = this.hashCode();
    }

    @Override
    public EnclaveType getEnclaveType() {
        return this.enclaveType;
    }

    @Override
    public int getEnclaveID() {
        return this.enclaveID;
    }

    @Override
    public boolean isEnclaveDebuggable() {
        return this.isEnclaveDebuggable;
    }

    @Override
    public long getEnclaveEPCMemorySizeBytes() {
        return this.enclaveEPCMemorySizeBytes;
    }

    @Override
    public int getEnclaveMaxThreadsNumber() {
        return this.enclaveMaxThreadsNumber;
    }
}
