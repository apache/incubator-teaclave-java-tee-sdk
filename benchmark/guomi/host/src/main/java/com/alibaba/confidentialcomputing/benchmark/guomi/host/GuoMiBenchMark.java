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

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 2)
@Threads(8)
@Fork(1)
@State(value = Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class GuoMiBenchMark {

    @State(Scope.Benchmark)
    public static class MockJVMEnclave {
        private Enclave enclave = null;
        private SMService service = null;

        @Setup(Level.Trial)
        public void createEnclave() throws Exception {
            enclave = EnclaveFactory.create(EnclaveType.MOCK_IN_JVM);
            service = enclave.load(SMService.class).next();
        }

        public SMService getServiceInstance() {
            return this.service;
        }
    }

    @State(Scope.Benchmark)
    public static class MockSVMEnclave {
        private Enclave enclave = null;
        private SMService service = null;

        @Setup(Level.Trial)
        public void createEnclave() throws Exception {
            enclave = EnclaveFactory.create(EnclaveType.MOCK_IN_SVM);
            service = enclave.load(SMService.class).next();
        }

        public SMService getServiceInstance() {
            return this.service;
        }
    }

    @State(Scope.Benchmark)
    public static class TeeSDKEnclave {
        private Enclave enclave = null;
        private SMService service = null;

        @Setup(Level.Trial)
        public void createEnclave() throws Exception {
            enclave = EnclaveFactory.create(EnclaveType.TEE_SDK);
            service = enclave.load(SMService.class).next();
        }

        public SMService getServiceInstance() {
            return this.service;
        }
    }

    @State(Scope.Benchmark)
    public static class EmbeddedLibOSEnclave {
        private Enclave enclave = null;
        private SMService service = null;

        @Setup(Level.Trial)
        public void createEnclave() throws Exception {
            enclave = EnclaveFactory.create(EnclaveType.EMBEDDED_LIB_OS);
            service = enclave.load(SMService.class).next();
        }

        public SMService getServiceInstance() {
            return this.service;
        }
    }

    @Benchmark
    public void sm2TeeSDKBenchMark(TeeSDKEnclave enclave) throws Exception {
        enclave.getServiceInstance().sm2Service("abcd_ed123.t12y@haha.com");
    }

    @Benchmark
    public void sm2MockJVMBenchMark(MockJVMEnclave enclave) throws Exception {
        enclave.getServiceInstance().sm2Service("abcd_ed123.t12y@haha.com");
    }

    @Benchmark
    public void sm2MockSVMBenchMark(MockSVMEnclave enclave) throws Exception {
        enclave.getServiceInstance().sm2Service("abcd_ed123.t12y@haha.com");
    }

    @Benchmark
    public void sm2EmbeddedLibOSBenchMark(EmbeddedLibOSEnclave enclave) throws Exception {
        enclave.getServiceInstance().sm2Service("abcd_ed123.t12y@haha.com");
    }

    @Benchmark
    public void sm3MockJVMBenchMark(MockJVMEnclave enclave) throws Exception {
        enclave.getServiceInstance().sm3Service("Hello World!");
    }

    @Benchmark
    public void sm3MockSVMBenchMark(MockSVMEnclave enclave) throws Exception {
        enclave.getServiceInstance().sm3Service("Hello World!");
    }

    @Benchmark
    public void sm3TeeSDKBenchMark(TeeSDKEnclave enclave) throws Exception {
        enclave.getServiceInstance().sm3Service("Hello World!");
    }

    @Benchmark
    public void sm3EmbeddedLibOSBenchMark(EmbeddedLibOSEnclave enclave) throws Exception {
        enclave.getServiceInstance().sm3Service("Hello World!");
    }

    @Benchmark
    public void sm4MockJVMBenchMark(MockJVMEnclave enclave) throws Exception {
        enclave.getServiceInstance().sm4Service("word1, word2 word3@word4?word5.word6");
    }

    @Benchmark
    public void sm4MockSVMBenchMark(MockSVMEnclave enclave) throws Exception {
        enclave.getServiceInstance().sm4Service("word1, word2 word3@word4?word5.word6");
    }

    @Benchmark
    public void sm4TeeSDKBenchMark(TeeSDKEnclave enclave) throws Exception {
        enclave.getServiceInstance().sm4Service("word1, word2 word3@word4?word5.word6");
    }

    @Benchmark
    public void sm4EmbeddedLibOSBenchMark(EmbeddedLibOSEnclave enclave) throws Exception {
        enclave.getServiceInstance().sm4Service("word1, word2 word3@word4?word5.word6");
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(GuoMiBenchMark.class.getSimpleName())
                .result("guomi_benchmark.json")
                .resultFormat(ResultFormatType.JSON).build();
        new Runner(opt).run();
    }
}
