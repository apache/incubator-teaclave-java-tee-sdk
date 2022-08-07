package com.alibaba.confidentialcomputing.host;

import com.alibaba.confidentialcomputing.host.exception.MetricTraceLogWriteException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public abstract class MetricTrace implements AutoCloseable {
    private final static String PRIORITY_METRIC_LOG_PATH = "com.alibaba.enclave.metric.path";
    private final static String PRIORITY_ENABLE_METRIC_LOG = "com.alibaba.enclave.metric.on";

    private static boolean enableEnclaveMetricTrace = false;
    private final static String DEFAULT_METRIC_LOG_PATH =
            "JavaEnclave_Metric_Log_" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + ".log";
    private static volatile String logPath;
    private static volatile BufferedWriter logFile;
    private static DecimalFormat formatter = new DecimalFormat("###,###");

    private final long start = System.nanoTime();

    static {
        String metricLogFlag = System.getProperty(PRIORITY_ENABLE_METRIC_LOG);
        if ("true".equals(metricLogFlag) || "1".equals(metricLogFlag)) {
            enableEnclaveMetricTrace = true;
        }
        String priorityLogPath = System.getProperty(PRIORITY_METRIC_LOG_PATH);
        if (priorityLogPath != null) {
            logPath = priorityLogPath;
        } else {
            logPath = DEFAULT_METRIC_LOG_PATH;
        }
    }

    public static void setEnclaveMetricTraceSwitch(boolean flag) {
        enableEnclaveMetricTrace = flag;
    }

    public static boolean isEnableEnclaveMetricTrace() {
        return enableEnclaveMetricTrace;
    }

    abstract EnclaveInfo getEnclaveInfo();

    abstract String getMetricKeyName();

    abstract long getCostInnerEnclave();

    void metricTracing(EnclaveInfo enclaveInfo, String name, long costTotal, long costEnclave) throws IOException {
        logFile.write(String.format("%s  %s  %s  %s  %s\r\n",
                enclaveInfo.getEnclaveID(),
                enclaveInfo.getEnclaveType(),
                name,
                formatter.format(TimeUnit.NANOSECONDS.toMicros(costTotal)),
                formatter.format(TimeUnit.NANOSECONDS.toMicros(costEnclave))));
        logFile.flush();
    }

    @Override
    public void close() throws MetricTraceLogWriteException {
        try {
            if (isEnableEnclaveMetricTrace()) {
                if (logFile == null) {
                    logFile = new BufferedWriter(new FileWriter(this.logPath));
                }
                metricTracing(getEnclaveInfo(), getMetricKeyName(), System.nanoTime() - start, getCostInnerEnclave());
            }
        } catch (IOException e) {
            throw new MetricTraceLogWriteException(e);
        }
    }
}
