package com.alibaba.enclave.bouncycatsle;

import com.oracle.svm.core.option.HostedOptionKey;
import org.graalvm.compiler.options.Option;
import org.graalvm.compiler.options.OptionType;

public class BCOptions {
    @Option(help = "When true, register the org.bouncycastle.jce.provider.BouncyCastleProvider.", type = OptionType.User)//
    public static final HostedOptionKey<Boolean> RegisterBCProvider = new HostedOptionKey<>(true);
}
