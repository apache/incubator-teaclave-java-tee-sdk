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

final class MetricTraceContext extends MetricTrace {
    private EnclaveInfo info;
    private long costInnerEnclave = 0x0;
    private final String pattern;

    MetricTraceContext(EnclaveInfo info, LogPrefix prefix) {
        this.info = info;
        pattern = prefix.toString();
    }

    MetricTraceContext(EnclaveInfo info, LogPrefix prefix, String service) {
        this.info = info;
        pattern = prefix.toString() + ":" + service;
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
