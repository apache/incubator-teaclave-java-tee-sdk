package com.alibaba.confidentialcomputing.host;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

final class EmbeddedLibOSEnclaveConfigure {
    private final static String AGENT_HTTP_THREAD_POOL_SIZE_PROPERTY = "com.alibaba.enclave.agent.thread.pool.size";
    private final static String ENCLAVE_STARTUP_TIMEOUT_MS_PROPERTY = "com.alibaba.enclave.embedded.startup.timeout_ms";
    private final static String ENCLAVE_DEBUG_LOG_LEVEL_PROPERTY = "com.alibaba.enclave.embedded.log.level";
    private final static String ENCLAVE_JVM_ARGS_PROPERTY = "com.alibaba.enclave.embedded.jvm.args";
    private final static String AGENT_HTTP_KEEP_ALIVE_TIMEOUT_S_PROPERTY = "com.alibaba.enclave.embedded.keepalive.timeout_s";
    private final static String AGENT_HTTP_KEEP_ALIVE_POOL_SIZE_PROPERTY = "com.alibaba.enclave.embedded.keepalive.max";
    private final static String AGENT_HTTP_CONNECT_TIMEOUT_MS_PROPERTY = "com.alibaba.enclave.embedded.connect.timeout_ms";
    private final static String AGENT_HTTP_READ_TIMEOUT_MS_PROPERTY = "com.alibaba.enclave.embedded.read.timeout_ms";
    private final static String AGENT_HTTP_READ_REMOTE_ATTESTATION_TIMEOUT_PROPERTY = "com.alibaba.enclave.embedded.ra.timeout_ms";

    private static EnclaveConfigure enclaveConfigure;
    private static EmbeddedLibOSEnclaveConfigure instance;

    private int threadPoolSize = 5;
    private int startupTimeout = (int) TimeUnit.MINUTES.toMillis(1); // ms.
    private String logLevel = "off";
    private String[] enclaveJVMArgs = new String[]{"-Dsun.net.httpserver.nodelay=true", "-XX:-UseCompressedOops", "-Xmx800m", "-Dos.name=Linux"};
    private int agentHttpConnectTimeout = 1000; // ms.
    private int agentHttpReadTimeout = 2000;    // ms.
    private int agentHttpRATimeout = 10_000;    // ms.
    private int agentHttpKeepAliveTimeout = 60 * 5; // s.
    private int agentHttpKeepAliveMax = 100;

    private EmbeddedLibOSEnclaveConfigure() throws IOException {
        enclaveConfigure = EnclaveConfigure.getInstance();
        parseProperty();
    }

    static EmbeddedLibOSEnclaveConfigure getInstance() throws IOException {
        if (instance != null) return instance;
        synchronized (EmbeddedLibOSEnclaveConfigure.class) {
            if (instance == null) instance = new EmbeddedLibOSEnclaveConfigure();
        }
        return instance;
    }

    private void parseProperty() {
        parseThreadPoolSize(System.getProperty(AGENT_HTTP_THREAD_POOL_SIZE_PROPERTY));
        parseStartupTimeout(System.getProperty(ENCLAVE_STARTUP_TIMEOUT_MS_PROPERTY));
        parseLogLevel(System.getProperty(ENCLAVE_DEBUG_LOG_LEVEL_PROPERTY));
        parseEnclaveJVMArgs(System.getProperty(ENCLAVE_JVM_ARGS_PROPERTY));
        parseHttpConnectTimeout(System.getProperty(AGENT_HTTP_CONNECT_TIMEOUT_MS_PROPERTY));
        parseHttpReadTimeout(System.getProperty(AGENT_HTTP_READ_TIMEOUT_MS_PROPERTY));
        parseHttpRATimeout(System.getProperty(AGENT_HTTP_READ_REMOTE_ATTESTATION_TIMEOUT_PROPERTY));
        parseHttpKeepAliveTimeout(System.getProperty(AGENT_HTTP_KEEP_ALIVE_TIMEOUT_S_PROPERTY));
        parseHttpKeepAliveMax(System.getProperty(AGENT_HTTP_KEEP_ALIVE_POOL_SIZE_PROPERTY));
    }

    private void parseThreadPoolSize(String size) {
        if (size != null) this.threadPoolSize = Integer.parseInt(size);
    }

    private void parseStartupTimeout(String timeout) {
        if (timeout != null) this.startupTimeout = Integer.parseInt(timeout);
    }

    private void parseLogLevel(String logLevel) {
        if (logLevel != null) this.logLevel = logLevel;
    }

    private void parseEnclaveJVMArgs(String args) {
        if (args != null) this.enclaveJVMArgs = Arrays.stream(args.split(",")).map(String::trim).toArray(String[]::new);
    }

    private void parseHttpConnectTimeout(String args) {
        if (args != null) this.agentHttpConnectTimeout = Integer.parseInt(args);
    }

    private void parseHttpReadTimeout(String args) {
        if (args != null) this.agentHttpReadTimeout = Integer.parseInt(args);
    }

    private void parseHttpRATimeout(String args) {
        if (args != null) this.agentHttpRATimeout = Integer.parseInt(args);
    }

    private void parseHttpKeepAliveTimeout(String args) {
        if (args != null) this.agentHttpKeepAliveTimeout = Integer.parseInt(args);
    }

    private void parseHttpKeepAliveMax(String args) {
        if (args != null) this.agentHttpKeepAliveMax = Integer.parseInt(args);
    }

    boolean isEnclaveDebuggable() {
        return enclaveConfigure.isEnclaveDebuggable();
    }

    boolean isEnableMetricTrace() {
        return enclaveConfigure.isEnableMetricTrace();
    }

    int getMaxEnclaveThreadNum() {
        return enclaveConfigure.getMaxEnclaveThreadNum();
    }

    int getEnclaveAgentThreadPoolSize() {
        return threadPoolSize;
    }

    int getEnclaveStartupTimeout() {
        return startupTimeout;
    }

    int getAgentHttpConnectTimeout() {
        return agentHttpConnectTimeout;
    }

    int getAgentHttpReadTimeout() {
        return agentHttpReadTimeout;
    }

    int getAgentHttpRATimeout() {
        return agentHttpRATimeout;
    }

    int getAgentHttpKeepAliveTimeout() {
        return agentHttpKeepAliveTimeout;
    }

    int getAgentHttpKeepAliveMax() {
        return agentHttpKeepAliveMax;
    }

    long getMaxEnclaveEPCMemorySizeBytes() {
        return enclaveConfigure.getMaxEnclaveEPCMemorySizeBytes();
    }

    String getLogLevel() {
        return logLevel;
    }

    String getMetricTraceFilePath() {
        return enclaveConfigure.getMetricTraceFilePath();
    }

    String[] getEnclaveJVMArgs() {
        return enclaveJVMArgs;
    }

    EnclaveType getDefaultEnclaveType() {
        return enclaveConfigure.getDefaultEnclaveType();
    }
}
