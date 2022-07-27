package com.alibaba.confidentialcomputing.host;

import javax.management.ConstructorParameters;

class SGXEnclaveInfo implements EnclaveInfo {
    private final EnclaveType enclaveType;
    private boolean isEnclaveDebuggable;
    private long enclaveEPCMemorySizeBytes; // Bytes.
    private int enclaveMaxThreadsNumber;

    @ConstructorParameters({"enclaveType", "isEnclaveDebuggable", "enclaveEPCMemorySizeBytes", "enclaveMaxThreadsNumber"})
    SGXEnclaveInfo(EnclaveType enclaveType, boolean isEnclaveDebuggable, long enclaveEPCMemorySizeBytes, int enclaveMaxThreadsNumber) {
        this.enclaveType = enclaveType;
        this.isEnclaveDebuggable = isEnclaveDebuggable;
        this.enclaveEPCMemorySizeBytes = enclaveEPCMemorySizeBytes;
        this.enclaveMaxThreadsNumber = enclaveMaxThreadsNumber;
    }

    @Override
    public EnclaveType getEnclaveType() {
        return this.enclaveType;
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
