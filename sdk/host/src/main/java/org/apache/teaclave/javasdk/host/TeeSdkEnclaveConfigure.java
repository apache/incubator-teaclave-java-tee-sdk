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

final class TeeSdkEnclaveConfigure {
    private final static long KB = 1024;
    private final static long MB = KB * 1024;
    private final static String TEE_SDK_SVM_MAX_HEAP_SIZE_PROPERTY = "org.apache.teaclave.javasdk.enclave.teesdk.maxheap_MB";
    private final static String TEE_SDK_SYMBOL_TRACE_ENABLE_PROPERTY = "org.apache.teaclave.javasdk.enclave.teesdk.symbol.trace";
    private static EnclaveConfigure enclaveConfigure;
    private static TeeSdkEnclaveConfigure teeSdkEnclaveConfigure;

    private int flag = 0x0;
    private long enclaveSVMMaxHeapSize = 0;

    private TeeSdkEnclaveConfigure() throws IOException {
        enclaveConfigure = EnclaveConfigure.getInstance();
        parseSymbolTraceEnable(System.getProperty(TEE_SDK_SYMBOL_TRACE_ENABLE_PROPERTY));
        parseAndInitSVMaxHeapSize(System.getProperty(TEE_SDK_SVM_MAX_HEAP_SIZE_PROPERTY));
    }

    static TeeSdkEnclaveConfigure getInstance() throws IOException {
        if (teeSdkEnclaveConfigure != null) return teeSdkEnclaveConfigure;
        synchronized (TeeSdkEnclaveConfigure.class) {
            if (teeSdkEnclaveConfigure == null) teeSdkEnclaveConfigure = new TeeSdkEnclaveConfigure();
        }
        return teeSdkEnclaveConfigure;
    }

    private void parseSymbolTraceEnable(String flag) {
        this.flag = Boolean.parseBoolean(flag) ? 1 : 0;
    }

    private void parseAndInitSVMaxHeapSize(String heapSize) {
        // make sure that svmMaxHeapSize should not larger than enclave_epc_memory * 0.8
        if (heapSize != null) {
            enclaveSVMMaxHeapSize = enclaveConfigure.getReferenceEnclaveMaxHeapSize();
            long confMaxHeapSize = Long.parseLong(heapSize) * MB;
            if (enclaveSVMMaxHeapSize > confMaxHeapSize) enclaveSVMMaxHeapSize = confMaxHeapSize;
        }
    }

    boolean isEnclaveDebuggable() {
        return enclaveConfigure.isEnclaveDebuggable();
    }

    boolean isEnableMetricTrace() {
        return enclaveConfigure.isEnableMetricTrace();
    }

    int isEnableTeeSDKSymbolTracing() {
        return flag;
    }

    int getMaxEnclaveThreadNum() {
        return enclaveConfigure.getMaxEnclaveThreadNum();
    }

    long getMaxEnclaveEPCMemorySizeBytes() {
        return enclaveConfigure.getMaxEnclaveEPCMemorySizeBytes();
    }

    long getEnclaveSVMMaxHeapSize() {
        return enclaveSVMMaxHeapSize;
    }

    String getMetricTraceFilePath() {
        return enclaveConfigure.getMetricTraceFilePath();
    }

    EnclaveType getDefaultEnclaveType() {
        return enclaveConfigure.getDefaultEnclaveType();
    }
}
