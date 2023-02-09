package org.apache.teaclave.javasdk.benchmark.string.host;

import org.apache.teaclave.javasdk.benchmark.string.common.StringOperationMetric;
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
@Measurement(iterations = 4, time = 2)
@Threads(4)
@Fork(1)
@State(value = Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class StringBenchMark {

    @Param(value = {"MOCK_IN_JVM", "MOCK_IN_SVM", "TEE_SDK"})
    private String enclaveServiceInstance;
    @Param(value = {"regex", "concat", "split"})
    private String stringOpt;

    @State(Scope.Thread)
    public static class EnclaveBenchmark {
        private Enclave mockJVMEnclave = null;
        private StringOperationMetric mockJVMService = null;
        private Enclave mockSVMEnclave = null;
        private StringOperationMetric mockSVMService = null;
        private Enclave teeSDKEnclave = null;
        private StringOperationMetric teeSDKService = null;
        private Enclave embeddedLibOSEnclave = null;
        private StringOperationMetric embeddedLibOSService = null;

        @Setup
        public void createEnclave() throws Exception {
            mockJVMEnclave = EnclaveFactory.create(EnclaveType.MOCK_IN_JVM);
            mockJVMService = mockJVMEnclave.load(StringOperationMetric.class).next();
            mockSVMEnclave = EnclaveFactory.create(EnclaveType.MOCK_IN_SVM);
            mockSVMService = mockSVMEnclave.load(StringOperationMetric.class).next();
            teeSDKEnclave = EnclaveFactory.create(EnclaveType.TEE_SDK);
            teeSDKService = teeSDKEnclave.load(StringOperationMetric.class).next();
        }

        @TearDown
        public void destroyEnclave() throws Exception {
            mockJVMEnclave.destroy();
            mockSVMEnclave.destroy();
            teeSDKEnclave.destroy();
        }

        public StringOperationMetric getMockJVMServiceInstance() {
            return mockJVMService;
        }

        public StringOperationMetric getMockSVMServiceInstance() {
            return mockSVMService;
        }

        public StringOperationMetric getTeeSDKServiceInstance() {
            return teeSDKService;
        }
    }

    private void stringBenchMarkImpl(EnclaveBenchmark enclave, String enclaveServiceInstance, String stringOpt) {
        StringOperationMetric service = null;
        switch (enclaveServiceInstance) {
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

        int regexWeight = 5000;
        int concatWeight = 50_000;
        int splitWeight = 5000;
        String regexContext = "abcd_ed123.t12y@haha.com";
        String regexPattern = "^[\\w._]+@\\w+\\.[a-zA-Z]+$";
        String concatContext = "Hello World!";
        String concatPattern = "abc";
        String splitContext = "word1, word2 word3@word4?word5.word6";
        String splitPattern = "[, ?.@]+";
        switch (stringOpt) {
            case "regex":
                Objects.requireNonNull(service).stringRegex(regexContext, regexPattern, regexWeight);
                break;
            case "concat":
                Objects.requireNonNull(service).stringConcat(concatContext, concatPattern, concatWeight);
                break;
            case "split":
                Objects.requireNonNull(service).stringSplit(splitContext, splitPattern, splitWeight);
                break;
        }
    }

    @Benchmark
    public void stringBenchMark(EnclaveBenchmark enclave) {
        stringBenchMarkImpl(enclave, enclaveServiceInstance, stringOpt);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(StringBenchMark.class.getSimpleName())
                .result("string_benchmark.json")
                .resultFormat(ResultFormatType.JSON).build();
        new Runner(opt).run();
    }
}
