package com.alibaba.confidentialcomputing.host;

import java.io.IOException;

final class MockInSvmEnclaveConfigure {
    private final static long KB = 1024;
    private final static long MB = KB * 1024;
    private final static String MOCK_IN_SVM_MAX_HEAP_SIZE_PROPERTY = "com.alibaba.enclave.mockinsvm.maxheap_MB";
    private static EnclaveConfigure enclaveConfigure;
    private static MockInSvmEnclaveConfigure mockInSvmEnclaveConfigure;

    private long enclaveSVMMaxHeapSize = 0;

    private MockInSvmEnclaveConfigure() throws IOException {
        enclaveConfigure = EnclaveConfigure.getInstance();
        parseAndInitSVMaxHeapSize(System.getProperty(MOCK_IN_SVM_MAX_HEAP_SIZE_PROPERTY));
    }

    static MockInSvmEnclaveConfigure getInstance() throws IOException {
        if (mockInSvmEnclaveConfigure != null) return mockInSvmEnclaveConfigure;
        synchronized (MockInSvmEnclaveConfigure.class) {
            if (mockInSvmEnclaveConfigure == null) mockInSvmEnclaveConfigure = new MockInSvmEnclaveConfigure();
        }
        return mockInSvmEnclaveConfigure;
    }

    private void parseAndInitSVMaxHeapSize(String heapSize) {
        if (heapSize != null) {
            enclaveSVMMaxHeapSize = enclaveConfigure.getReferenceEnclaveMaxHeapSize();
            long confMaxHeapSize = Long.parseLong(heapSize) * MB;
            // make sure that svmMaxHeapSize should not larger than enclave_epc_memory * 0.8
            if (enclaveSVMMaxHeapSize > confMaxHeapSize) enclaveSVMMaxHeapSize = confMaxHeapSize;
        }
    }

    long getEnclaveSVMMaxHeapSize() {
        return enclaveSVMMaxHeapSize;
    }
}
