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

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

final class EnclaveConfigure {
    private final static double RATIO = 0.8;
    private final static long KB = 1024;
    private final static long MB = KB * 1024;
    private final static long GB = MB * 1024;
    // TEE_SDK/EMBEDDED_LIB_OS/MOCK_IN_JVM/MOCK_IN_SVM
    private final static String ENCLAVE_TYPE_PROPERTY = "org.apache.teaclave.javasdk.enclave.type";
    private final static String ENCLAVE_DEBUG_PROPERTY = "org.apache.teaclave.javasdk.enclave.debuggable";
    private final static String METRIC_TRACE_LOG_FILE_PATH_PROPERTY = "org.apache.teaclave.javasdk.enclave.metric.log";
    private final static String METRIC_TRACE_ENABLE_PROPERTY = "org.apache.teaclave.javasdk.enclave.metric.enable";

    private final static String JAVA_ENCLAVE_CONFIG_FILE_TEMPLATE = "/opt/javaenclave/config/template/java_enclave_configure.json";
    private final static String JAVA_ENCLAVE_CONFIG_FILE = "java_enclave_configure.json";
    private final static String ENCLAVE_DEBUGGABLE_CONFIG_FILE_KEY = "debuggable";
    private final static String ENCLAVE_TYPE_CONFIG_FILE_KEY = "enclave_type";
    private final static String METRIC_TRACE_LOG_FILE_PATH_CONFIG_FILE_KEY = "metric_trace_file_path";
    private final static String METRIC_TRACE_ENABLE_CONFIG_FILE_KEY = "metric_trace_enable";
    private final static String ENCLAVE_MAX_THREAD_NUMBER_CONFIG_FILE_KEY = "enclave_max_thread";
    private final static String ENCLAVE_MAX_EPC_MEMORY_SIZE_CONFIG_FILE_KEY = "enclave_max_epc_memory_size_MB";
    private final static String DEFAULT_METRIC_LOG_PATH =
            "JavaEnclave_Metric_Log_" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + ".log";

    private static EnclaveConfigure enclaveConfigure;

    // default value without giving explicitly enclave type.
    // for example, EnclaveFactory.create() will create defaultEnclaveType enclave.
    private EnclaveType enclaveType = EnclaveType.TEE_SDK;
    // it's for TEE_SKD and EMBEDDED_LIB_OS.
    private boolean debuggable = false;
    // enable metric trace, default is disable.
    private boolean enableMetricTrace = false;
    // config metric trace file path.
    private String metricTraceFilePath = DEFAULT_METRIC_LOG_PATH;
    private int maxEnclaveThreadNum = 50;
    private long maxEnclaveEPCMemorySize = 1500 * MB;
    private long referenceEnclaveMaxHeapSize = (long) (maxEnclaveEPCMemorySize * RATIO);

    private EnclaveConfigure() throws IOException {
        // first update value from config file.
        parseTemplateConfigureFile();
        // second update value form user define config file.
        parseUserConfigureFile();
        // at last update value from property.
        // config file is low priority then java -D property setting.
        parseProperty();
    }

    static EnclaveConfigure getInstance() throws IOException {
        if (enclaveConfigure != null) return enclaveConfigure;
        synchronized (EnclaveConfigure.class) {
            if (enclaveConfigure == null) enclaveConfigure = new EnclaveConfigure();
        }
        return enclaveConfigure;
    }

    private void parseEnclaveType(String enclaveType) {
        // parse enclave type.
        if (enclaveType != null) this.enclaveType = EnclaveType.valueOf(enclaveType);
    }

    private boolean parseBooleanFlag(String flag) {
        return Boolean.parseBoolean(flag);
    }

    private void parseMetricTracingEnable(String metricEnable) {
        if (metricEnable != null) this.enableMetricTrace = parseBooleanFlag(metricEnable);
    }

    private void parseEnclaveDebuggable(String debuggable) {
        // parse enclave debuggable or not.
        if (debuggable != null) this.debuggable = parseBooleanFlag(debuggable);
    }

    private void parseMetricTracingLogPath(String filePath) {
        // parse metric log file path.
        if (filePath != null && !filePath.isBlank()) this.metricTraceFilePath = filePath;
    }

    private void parseProperty() {
        parseEnclaveType(System.getProperty(ENCLAVE_TYPE_PROPERTY));
        parseEnclaveDebuggable(System.getProperty(ENCLAVE_DEBUG_PROPERTY));
        parseMetricTracingEnable(System.getProperty(METRIC_TRACE_ENABLE_PROPERTY));
        parseMetricTracingLogPath(System.getProperty(METRIC_TRACE_LOG_FILE_PATH_PROPERTY));
    }

    private void parseConfigureFile(String path) throws IOException {
        File file = new File(path);
        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        JSONObject jsonObject = new JSONObject(content);
        this.debuggable = jsonObject.getBoolean(ENCLAVE_DEBUGGABLE_CONFIG_FILE_KEY);
        parseEnclaveType(jsonObject.getString(ENCLAVE_TYPE_CONFIG_FILE_KEY));
        this.enableMetricTrace = jsonObject.getBoolean(METRIC_TRACE_ENABLE_CONFIG_FILE_KEY);
        parseMetricTracingLogPath(jsonObject.getString(METRIC_TRACE_LOG_FILE_PATH_CONFIG_FILE_KEY));
        this.maxEnclaveThreadNum = jsonObject.getInt(ENCLAVE_MAX_THREAD_NUMBER_CONFIG_FILE_KEY);
        this.maxEnclaveEPCMemorySize = jsonObject.getInt(ENCLAVE_MAX_EPC_MEMORY_SIZE_CONFIG_FILE_KEY) * MB;
        this.referenceEnclaveMaxHeapSize = (long) (this.maxEnclaveEPCMemorySize * RATIO);
    }

    private void parseTemplateConfigureFile() throws IOException {
        parseConfigureFile(JAVA_ENCLAVE_CONFIG_FILE_TEMPLATE);
    }

    private void parseUserConfigureFile() throws IOException {
        // only parse configure file when it exists in .jar.
        if (ExtractLibrary.isFileExist(EnclaveConfigure.class.getClassLoader(), JAVA_ENCLAVE_CONFIG_FILE)) {
            String configFilePath = ExtractLibrary.extractLibrary(EnclaveConfigure.class.getClassLoader(), JAVA_ENCLAVE_CONFIG_FILE);
            parseConfigureFile(configFilePath);
        }
    }

    boolean isEnclaveDebuggable() {
        return debuggable;
    }

    boolean isEnableMetricTrace() {
        return enableMetricTrace;
    }

    int getMaxEnclaveThreadNum() {
        return maxEnclaveThreadNum;
    }

    long getMaxEnclaveEPCMemorySizeBytes() {
        return maxEnclaveEPCMemorySize;
    }

    long getReferenceEnclaveMaxHeapSize() {
        return referenceEnclaveMaxHeapSize;
    }

    String getMetricTraceFilePath() {
        return metricTraceFilePath;
    }

    EnclaveType getDefaultEnclaveType() {
        return enclaveType;
    }
}
