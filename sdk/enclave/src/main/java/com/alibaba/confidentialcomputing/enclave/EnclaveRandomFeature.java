package com.alibaba.confidentialcomputing.enclave;

import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.hosted.FeatureHandler;
import com.oracle.svm.hosted.FeatureImpl;
import org.graalvm.nativeimage.hosted.Feature;

@AutomaticFeature
public class EnclaveRandomFeature implements Feature {
    @Override
    public void afterRegistration(Feature.AfterRegistrationAccess access) {
        if (EnclaveOptions.RunInEnclave.getValue()) {
            FeatureImpl.AfterRegistrationAccessImpl a = (FeatureImpl.AfterRegistrationAccessImpl) access;
            FeatureHandler featureHandler = a.getFeatureHandler();
            EnclavePlatFormSettings.disableFeatures(featureHandler, "com.oracle.svm.core.posix.NativeSecureRandomFilesCloser");
        }
    }
}
