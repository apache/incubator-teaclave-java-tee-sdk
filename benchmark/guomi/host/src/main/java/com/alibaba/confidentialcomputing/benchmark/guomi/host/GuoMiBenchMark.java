package com.alibaba.confidentialcomputing.benchmark.guomi.host;

import com.alibaba.confidentialcomputing.benchmark.guomi.common.SMService;
import com.alibaba.confidentialcomputing.host.Enclave;
import com.alibaba.confidentialcomputing.host.EnclaveFactory;
import com.alibaba.confidentialcomputing.host.EnclaveType;
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

    @Param(value = {"MOCK_IN_JVM", "MOCK_IN_SVM", "TEE_SDK", "EMBEDDED_LIB_OS"})
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
        private Enclave embeddedLibOSEnclave = null;
        private SMService embeddedLibOSService = null;

        @Setup
        public void createEnclave() throws Exception {
            mockJVMEnclave = EnclaveFactory.create(EnclaveType.MOCK_IN_JVM);
            mockJVMService = mockJVMEnclave.load(SMService.class).next();
            mockSVMEnclave = EnclaveFactory.create(EnclaveType.MOCK_IN_SVM);
            mockSVMService = mockSVMEnclave.load(SMService.class).next();
            teeSDKEnclave = EnclaveFactory.create(EnclaveType.TEE_SDK);
            teeSDKService = teeSDKEnclave.load(SMService.class).next();
            embeddedLibOSEnclave = EnclaveFactory.create(EnclaveType.EMBEDDED_LIB_OS);
            embeddedLibOSService = embeddedLibOSEnclave.load(SMService.class).next();
        }

        @TearDown
        public void destroyEnclave() throws Exception {
            mockJVMEnclave.destroy();
            mockSVMEnclave.destroy();
            teeSDKEnclave.destroy();
            embeddedLibOSEnclave.destroy();
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

        public SMService getEmbeddedLibOSServiceInstance() {
            return embeddedLibOSService;
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
            case "EMBEDDED_LIB_OS":
                service = enclave.getEmbeddedLibOSServiceInstance();
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
