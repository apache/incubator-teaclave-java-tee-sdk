package com.alibaba.confidentialcomputing.enclave;

import com.oracle.svm.core.option.HostedOptionKey;
import org.graalvm.compiler.options.Option;
import org.graalvm.compiler.options.OptionType;

public class EnclaveOptions {
    @Option(help = "Use native function instead of accessing /dev/random /dev/urandom for getting random number.", type = OptionType.User)
//
    public static final HostedOptionKey<Boolean> UseNativeGetRandom = new HostedOptionKey<>(true);

    @Option(help = "Enable enclave features.", type = OptionType.User)
//
    public static final HostedOptionKey<Boolean> RunInEnclave = new HostedOptionKey<>(true);
}
