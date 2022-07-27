package com.alibaba.confidentialcomputing.host;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.alibaba.confidentialcomputing.common.exception.ConfidentialComputingException;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.alibaba.confidentialcomputing.host.ExtractLibrary.extractLibrary;

class EmbeddedLibOSEnclaveConfig {
    private final static String EMBEDDED_LIB_OS_ENCLAVE_CONFIG_FILE = "embedded_libos_enclave.json";
    private static String configFilePath;
    private static EmbeddedLibOSEnclaveConfig config;

    private boolean debuggable = false;
    private int agentHttpHandlerThreadPoolSize = 5;
    private int embeddedLibOSEnclaveStartupDuration = (int) TimeUnit.MINUTES.toMillis(1);
    private int maxNumOfThreads = 50;
    private long maxEPCHeapSize = 1500;
    private String libOSLogLevel = "off";
    private String[] enclaveJVMArgs = null;

    static {
        try {
            configFilePath = extractLibrary(EmbeddedLibOSEnclave.class.getClassLoader(), EMBEDDED_LIB_OS_ENCLAVE_CONFIG_FILE);
            File file = new File(configFilePath);
            String content = Files.readString(file.toPath(), Charset.forName("UTF-8"));
            JSONObject jsonObject = new JSONObject(content);
            boolean debuggable = jsonObject.getBoolean("debuggable");
            int agentHttpHandlerThreadPoolSize = jsonObject.getInt("agent_http_handler_thread_pool_size");
            int embeddedLibOSEnclaveStartupDuration = jsonObject.getInt("enclave_startup_duration_ms");
            int maxNumOfThreads = jsonObject.getInt("max_num_of_threads");
            long maxEPCHeapSize = parseHeapSize(jsonObject.getString("user_space_size")) * 1024 * 1024; // convert MB to B.
            String libOSLogLevel = jsonObject.getString("log_level");
            JSONArray jvmArgs = jsonObject.getJSONArray("enclave_jvm_args");
            List<String> jvmArgsList = new ArrayList<>();
            for (int i = 0; i < jvmArgs.length(); i++) {
                jvmArgsList.add(jvmArgs.getString(i));
            }
            String[] enclaveJVMArgs = jvmArgsList.toArray(new String[jvmArgsList.size()]);
            config = new EmbeddedLibOSEnclaveConfig(
                    debuggable,
                    maxNumOfThreads,
                    maxEPCHeapSize,
                    agentHttpHandlerThreadPoolSize,
                    embeddedLibOSEnclaveStartupDuration,
                    libOSLogLevel,
                    enclaveJVMArgs);
        } catch (IOException | ConfidentialComputingException e) {
            e.printStackTrace();
        }
    }

    private static long parseHeapSize(String value) throws ConfidentialComputingException {
        // check heap size dimension must be MB or mb.
        if (!(value.endsWith("MB") || value.endsWith("mb"))) {
            throw new ConfidentialComputingException("Embedded lib os heap size dimension must be MB.");
        }
        String trimValue = value.replace("MB", "");
        if (trimValue.startsWith("0x") || trimValue.startsWith("0X")) {
            return Long.parseLong(trimValue.substring(2), 16);
        }
        return Long.parseLong(trimValue);
    }

    static EmbeddedLibOSEnclaveConfig getEmbeddedLibOSEnclaveConfigInstance() {
        return config;
    }

    private EmbeddedLibOSEnclaveConfig(
            boolean debuggable,
            int maxNumOfThreads,
            long maxEPCHeapSize,
            int agentHttpHandlerThreadPoolSize,
            int embeddedLibOSEnclaveStartupDuration,
            String libOSLogLevel,
            String[] jvmArgs) {
        this.debuggable = debuggable;
        this.maxNumOfThreads = maxNumOfThreads;
        this.maxEPCHeapSize = maxEPCHeapSize;
        this.agentHttpHandlerThreadPoolSize = agentHttpHandlerThreadPoolSize;
        this.embeddedLibOSEnclaveStartupDuration = embeddedLibOSEnclaveStartupDuration;
        this.libOSLogLevel = libOSLogLevel;
        this.enclaveJVMArgs = jvmArgs;
    }

    EnclaveDebug getDebuggable() {
        if (this.debuggable) {
            return EnclaveDebug.DEBUG;
        }
        return EnclaveDebug.RELEASE;
    }

    int getAgentHttpHandlerThreadPoolSize() {
        return this.agentHttpHandlerThreadPoolSize;
    }

    int getEmbeddedLibOSEnclaveStartupDuration() {
        return this.embeddedLibOSEnclaveStartupDuration;
    }

    int getMaxNumOfThreads() {
        return this.maxNumOfThreads;
    }

    long getMaxEPCHeapSizeBytes() {
        return this.maxEPCHeapSize;
    }

    String getLibOSLogLevel() {
        return this.libOSLogLevel;
    }

    String[] getEnclaveJVMArgs() {
        return this.enclaveJVMArgs;
    }
}
