package com.alibaba.confidentialcomputing.host;

final class MetricTraceContext extends MetricTrace {
    private EnclaveInfo info;
    private long costInnerEnclave = 0x0;
    private String pattern;

    MetricTraceContext(EnclaveInfo info, LogPrefix prefix) {
        this.info = info;
        pattern = prefix.toString();
    }

    MetricTraceContext(EnclaveInfo info, LogPrefix prefix, String service) {
        this.info = info;
        pattern = new StringBuilder().append(prefix.toString()).append(":").append(service).toString();
    }

    MetricTraceContext(LogPrefix prefix) {
        pattern = prefix.toString();
    }

    enum LogPrefix {
        METRIC_LOG_ENCLAVE_CREATING_PATTERN("enclave_creating_cost(us)"),
        METRIC_LOG_ENCLAVE_DESTROYING_PATTERN("enclave_destroying_cost(us)"),
        METRIC_LOG_ENCLAVE_SERVICE_LOADING_PATTERN("enclave_service_loading(us)"),
        METRIC_LOG_ENCLAVE_SERVICE_UNLOADING_PATTERN("enclave_service_unloading(us)"),
        METRIC_LOG_ENCLAVE_SERVICE_INVOKING_PATTERN("enclave_service_invoking(us)");
        private final String prefix;

        LogPrefix(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public String toString() {
            return this.prefix;
        }
    }

    void setCostInnerEnclave(long cost) {
        costInnerEnclave = cost;
    }

    void setEnclaveInfo(EnclaveInfo info) {
        this.info = info;
    }

    @Override
    EnclaveInfo getEnclaveInfo() {
        return this.info;
    }

    @Override
    String getMetricKeyName() {
        return pattern;
    }

    @Override
    long getCostInnerEnclave() {
        return this.costInnerEnclave;
    }
}
