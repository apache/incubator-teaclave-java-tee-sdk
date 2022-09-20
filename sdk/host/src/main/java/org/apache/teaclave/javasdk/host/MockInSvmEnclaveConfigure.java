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

import java.io.IOException;

final class MockInSvmEnclaveConfigure {
    private final static long KB = 1024;
    private final static long MB = KB * 1024;
    private final static String MOCK_IN_SVM_MAX_HEAP_SIZE_PROPERTY = "org.apache.teaclave.javasdk.enclave.mockinsvm.maxheap_MB";
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
