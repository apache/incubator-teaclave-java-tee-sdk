package com.alibaba.confidentialcomputing.benchmark.string.host;

import com.alibaba.confidentialcomputing.benchmark.string.common.StringOperationMetric;
import com.alibaba.confidentialcomputing.host.Enclave;
import com.alibaba.confidentialcomputing.host.EnclaveFactory;
import com.alibaba.confidentialcomputing.host.EnclaveType;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 4, time = 2)
@Threads(8)
@Fork(1)
@State(value = Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class StringBenchMark {
    @Param(value = {"5"})
    private int iterator;

    @State(Scope.Benchmark)
    public static class MockJVMEnclave {
        private Enclave enclave = null;
        private StringOperationMetric service = null;

        @Setup(Level.Trial)
        public void createEnclave() throws Exception {
            enclave = EnclaveFactory.create(EnclaveType.MOCK_IN_JVM);
            service = enclave.load(StringOperationMetric.class).next();
        }

        public StringOperationMetric getServiceInstance() {
            return this.service;
        }
    }

    @State(Scope.Benchmark)
    public static class MockSVMEnclave {
        private Enclave enclave = null;
        private StringOperationMetric service = null;

        @Setup(Level.Trial)
        public void createEnclave() throws Exception {
            enclave = EnclaveFactory.create(EnclaveType.MOCK_IN_SVM);
            service = enclave.load(StringOperationMetric.class).next();
        }

        public StringOperationMetric getServiceInstance() {
            return this.service;
        }
    }

    @State(Scope.Benchmark)
    public static class TeeSDKEnclave {
        private Enclave enclave = null;
        private StringOperationMetric service = null;

        @Setup(Level.Trial)
        public void createEnclave() throws Exception {
            enclave = EnclaveFactory.create(EnclaveType.TEE_SDK);
            service = enclave.load(StringOperationMetric.class).next();
        }

        public StringOperationMetric getServiceInstance() {
            return this.service;
        }
    }

    @State(Scope.Benchmark)
    public static class EmbeddedLibOSEnclave {
        private Enclave enclave = null;
        private StringOperationMetric service = null;

        @Setup(Level.Trial)
        public void createEnclave() throws Exception {
            enclave = EnclaveFactory.create(EnclaveType.EMBEDDED_LIB_OS);
            service = enclave.load(StringOperationMetric.class).next();
        }

        public StringOperationMetric getServiceInstance() {
            return this.service;
        }
    }

    @Benchmark
    public void stringRegexMockJVMBenchMark(MockJVMEnclave enclave) {
        enclave.getServiceInstance().stringRegex("abcd_ed123.t12y@haha.com", "^[\\w._]+@\\w+\\.[a-zA-Z]+$", iterator);
    }

    @Benchmark
    public void stringRegexMockSVMBenchMark(MockSVMEnclave enclave) {
        enclave.getServiceInstance().stringRegex("abcd_ed123.t12y@haha.com", "^[\\w._]+@\\w+\\.[a-zA-Z]+$", iterator);
    }

    @Benchmark
    public void stringRegexTeeSDKBenchMark(TeeSDKEnclave enclave) {
        enclave.getServiceInstance().stringRegex("abcd_ed123.t12y@haha.com", "^[\\w._]+@\\w+\\.[a-zA-Z]+$", iterator);
    }

    @Benchmark
    public void stringRegexEmbeddedLibOSBenchMark(EmbeddedLibOSEnclave enclave) {
        enclave.getServiceInstance().stringRegex("abcd_ed123.t12y@haha.com", "^[\\w._]+@\\w+\\.[a-zA-Z]+$", iterator);
    }

    @Benchmark
    public void stringConcatMockJVMBenchMark(MockJVMEnclave enclave) {
        enclave.getServiceInstance().stringConcat("Hello World!", "abc", iterator);
    }

    @Benchmark
    public void stringConcatMockSVMBenchMark(MockSVMEnclave enclave) {
        enclave.getServiceInstance().stringConcat("Hello World!", "abc", iterator);
    }

    @Benchmark
    public void stringConcatTeeSDKBenchMark(TeeSDKEnclave enclave) {
        enclave.getServiceInstance().stringConcat("Hello World!", "abc", iterator);
    }

    @Benchmark
    public void stringConcatEmbeddedLibOSBenchMark(EmbeddedLibOSEnclave enclave) {
        enclave.getServiceInstance().stringConcat("Hello World!", "abc", iterator);
    }

    @Benchmark
    public void stringSplitMockJVMBenchMark(MockJVMEnclave enclave) {
        enclave.getServiceInstance().stringSplit("word1, word2 word3@word4?word5.word6", "[, ?.@]+", iterator);
    }

    @Benchmark
    public void stringSplitMockSVMBenchMark(MockSVMEnclave enclave) {
        enclave.getServiceInstance().stringSplit("word1, word2 word3@word4?word5.word6", "[, ?.@]+", iterator);
    }

    @Benchmark
    public void stringSplitTeeSDKBenchMark(TeeSDKEnclave enclave) {
        enclave.getServiceInstance().stringSplit("word1, word2 word3@word4?word5.word6", "[, ?.@]+", iterator);
    }

    @Benchmark
    public void stringSplitEmbeddedLibOSBenchMark(EmbeddedLibOSEnclave enclave) {
        enclave.getServiceInstance().stringSplit("word1, word2 word3@word4?word5.word6", "[, ?.@]+", iterator);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(StringBenchMark.class.getSimpleName())
                .result("string_benchmark.json")
                .resultFormat(ResultFormatType.JSON).build();
        new Runner(opt).run();
    }
}
