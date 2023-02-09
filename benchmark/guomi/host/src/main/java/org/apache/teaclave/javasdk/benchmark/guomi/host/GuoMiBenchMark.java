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

package org.apache.teaclave.javasdk.benchmark.guomi.host;

import org.apache.teaclave.javasdk.benchmark.guomi.common.SMService;
import org.apache.teaclave.javasdk.host.Enclave;
import org.apache.teaclave.javasdk.host.EnclaveFactory;
import org.apache.teaclave.javasdk.host.EnclaveType;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 4, time = 1)
@Threads(4)
@Fork(1)
@State(value = Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class GuoMiBenchMark {

    public final String sm3Context = "Hello World!";

    @Param(value = {"MOCK_IN_JVM", "MOCK_IN_SVM", "TEE_SDK"})
    private String enclaveServiceInstance;
    @Param(value = {"SM2", "SM3", "SM4"})
    private String smAlgo;

    @State(Scope.Thread)
    public static class EnclaveBenchmark {
        private Enclave mockJVMEnclave = null;
        private SMService mockJVMService = null;
        private Enclave mockSVMEnclave = null;
        private SMService mockSVMService = null;
        private Enclave teeSDKEnclave = null;
        private SMService teeSDKService = null;

        @Setup
        public void createEnclave() throws Exception {
            mockJVMEnclave = EnclaveFactory.create(EnclaveType.MOCK_IN_JVM);
            mockJVMService = mockJVMEnclave.load(SMService.class).next();
            mockSVMEnclave = EnclaveFactory.create(EnclaveType.MOCK_IN_SVM);
            mockSVMService = mockSVMEnclave.load(SMService.class).next();
            teeSDKEnclave = EnclaveFactory.create(EnclaveType.TEE_SDK);
            teeSDKService = teeSDKEnclave.load(SMService.class).next();
        }

        @TearDown
        public void destroyEnclave() throws Exception {
            mockJVMEnclave.destroy();
            mockSVMEnclave.destroy();
            teeSDKEnclave.destroy();
        }

        public SMService getMockJVMServiceInstance() {
            return mockJVMService;
        }

        public SMService getMockSVMServiceInstance() {
            return mockSVMService;
        }

        public SMService getTeeSDKServiceInstance() {
            return teeSDKService;
        }
    }

    private void smBenchmarkImpl(EnclaveBenchmark enclave, String serviceName, String smAlgo) throws Exception {
        SMService service = null;
        switch (serviceName) {
            case "MOCK_IN_JVM":
                service = enclave.getMockJVMServiceInstance();
                break;
            case "MOCK_IN_SVM":
                service = enclave.getMockSVMServiceInstance();
                break;
            case "TEE_SDK":
                service = enclave.getTeeSDKServiceInstance();
                break;
        }

        int sm2Weight = 10;
        int sm3Weight = 20_000;
        int sm4Weight = 300;
        String sm2Context = "abcd_ed123.t12y@haha.com";
        String sm4Context = "word1, word2 word3@word4?word5.word6";
        switch (smAlgo) {
            case "SM2":
                Objects.requireNonNull(service).sm2Service(sm2Context, sm2Weight);
                break;
            case "SM3":
                Objects.requireNonNull(service).sm3Service(sm3Context, sm3Weight);
                break;
            case "SM4":
                Objects.requireNonNull(service).sm4Service(sm4Context, sm4Weight);
                break;
        }
    }

    @Benchmark
    public void smBenchMark(EnclaveBenchmark enclave) throws Exception {
        smBenchmarkImpl(enclave, enclaveServiceInstance, smAlgo);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(GuoMiBenchMark.class.getSimpleName())
                .result("guomi_benchmark.json")
                .resultFormat(ResultFormatType.JSON).build();
        new Runner(opt).run();
    }
}
