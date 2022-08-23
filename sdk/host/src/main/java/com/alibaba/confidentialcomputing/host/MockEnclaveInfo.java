package com.alibaba.confidentialcomputing.host;

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
