package com.alibaba.confidentialcomputing.enclave.cpufeatures;

import com.oracle.svm.core.amd64.AMD64CPUFeatureAccess;
import com.oracle.svm.core.annotate.Uninterruptible;
import jdk.vm.ci.amd64.AMD64;
import jdk.vm.ci.code.Architecture;

import java.util.EnumSet;

/**
 * Don't check CPU features in enclave environment because the native functions are not supported by enclave SDKs.
 *
 * @see EnclaveAMD64CPUFeatureAccessFeature
 * @since GraalVM 22.2.0
 */
public class EnclaveAMD64CPUFeatureAccess extends AMD64CPUFeatureAccess {
    public EnclaveAMD64CPUFeatureAccess(EnumSet<?> buildtimeCPUFeatures, int[] offsets, byte[] errorMessageBytes, byte[] buildtimeFeatureMaskBytes) {
        super(buildtimeCPUFeatures, offsets, errorMessageBytes, buildtimeFeatureMaskBytes);
    }

    @Override
    public EnumSet<AMD64.CPUFeature> determineHostCPUFeatures() {
        return super.determineHostCPUFeatures();
    }

    @Override
    @Uninterruptible(reason = "Thread state not set up yet.")
    public int verifyHostSupportsArchitectureEarly() {
        return 0;
    }

    @Override
    @Uninterruptible(reason = "Thread state not set up yet.")
    public void verifyHostSupportsArchitectureEarlyOrExit() {

    }

    @Override
    public void enableFeatures(Architecture runtimeArchitecture) {

    }
}
