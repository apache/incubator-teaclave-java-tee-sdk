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
    private static boolean enableEnclaveMetricTrace = false;
    private static volatile String logPath = "JavaEnclave_Metric_Log_" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + ".log";
    private static volatile BufferedWriter logFile;
    private static DecimalFormat formatter = new DecimalFormat("###,###");

    private final long start = System.nanoTime();

    static {
        try {
            boolean enableEnclaveMetricTraceTemp = EnclaveConfigure.getInstance().isEnableMetricTrace();
            String logPathTemp = EnclaveConfigure.getInstance().getMetricTraceFilePath();
            enableEnclaveMetricTrace = enableEnclaveMetricTraceTemp;
            logPath = logPathTemp;
        } catch (IOException e) {
            ; // if exception happen, use original init value.
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
                    synchronized (MetricTrace.class) {
                        if (logFile == null) {
                            logFile = new BufferedWriter(new FileWriter(this.logPath));
                        }
                    }
                }
                metricTracing(getEnclaveInfo(), getMetricKeyName(), System.nanoTime() - start, getCostInnerEnclave());
            }
        } catch (IOException e) {
            throw new MetricTraceLogWriteException(e);
        }
    }
}
