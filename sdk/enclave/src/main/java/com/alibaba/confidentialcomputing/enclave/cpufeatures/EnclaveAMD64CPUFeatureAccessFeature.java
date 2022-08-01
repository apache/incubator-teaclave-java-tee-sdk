package com.alibaba.confidentialcomputing.enclave.cpufeatures;

import com.alibaba.confidentialcomputing.enclave.EnclaveOptions;
import com.oracle.svm.core.amd64.AMD64CPUFeatureAccess;
import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.hosted.AMD64CPUFeatureAccessFeature;
import org.graalvm.nativeimage.Platform;
import org.graalvm.nativeimage.Platforms;

import java.util.EnumSet;

/**
 * {@link AMD64CPUFeatureAccessFeature} adds the {@link AMD64CPUFeatureAccess} instance into {@link org.graalvm.nativeimage.ImageSingletons},
 * while {@link AMD64CPUFeatureAccess} eventually calls enclave SDK unsupported CPU feature checking functions. So it will
 * lead to runtime crash in enclave environment.
 * This class disables {@link AMD64CPUFeatureAccessFeature} by extending it (as GraalVM always uses the most specific
 * Feature class see {@link com.oracle.svm.hosted.FeatureHandler#registerFeatures} for details), and provides
 * {@link EnclaveAMD64CPUFeatureAccess} instance instead to avoid calling the unsupported functions.
 * <p>
 * The unsupported functions are called by function {@code determineCPUFeatures} in {@code cpuid.c}.
 *
 * @since GraalVM 22.2.0
 */
@AutomaticFeature
@Platforms({Platform.AMD64.class})
public class EnclaveAMD64CPUFeatureAccessFeature extends AMD64CPUFeatureAccessFeature {

    @Override
    protected AMD64CPUFeatureAccess createCPUFeatureAccessSingleton(EnumSet<?> buildtimeCPUFeatures, int[] offsets, byte[] errorMessageBytes, byte[] buildtimeFeatureMaskBytes) {
        if (EnclaveOptions.RunInEnclave.getValue()) {
            return new EnclaveAMD64CPUFeatureAccess(buildtimeCPUFeatures, offsets, errorMessageBytes, buildtimeFeatureMaskBytes);
        } else {
            return super.createCPUFeatureAccessSingleton(buildtimeCPUFeatures, offsets, errorMessageBytes, buildtimeFeatureMaskBytes);
        }
    }
}
