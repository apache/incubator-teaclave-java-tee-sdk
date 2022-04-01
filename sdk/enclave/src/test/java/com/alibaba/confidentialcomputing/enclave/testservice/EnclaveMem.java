package com.alibaba.confidentialcomputing.enclave.testservice;

import com.oracle.svm.core.heap.PhysicalMemory;

public class EnclaveMem implements MemService{
    @Override
    public long getSize() {
        return PhysicalMemory.size().rawValue();
    }
}
