package com.alibaba.confidentialcomputing.enclave;

import com.oracle.graal.pointsto.util.AnalysisError;
import com.oracle.svm.core.util.VMError;
import com.oracle.svm.hosted.FeatureHandler;
import com.oracle.svm.hosted.ImageSingletonsSupportImpl;
import org.graalvm.nativeimage.hosted.Feature;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class EnclavePlatFormSettings {
    private static final DummyFeature DUMMY_FEATURE = new DummyFeature();

    private static final Field configObjectsField;

    static {
        try {
            configObjectsField = ImageSingletonsSupportImpl.HostedManagement.class.getDeclaredField("configObjects");
            configObjectsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw VMError.shouldNotReachHere(e);
        }
    }

    static class DummyFeature implements Feature {
    }

    public static void disableFeatures(FeatureHandler featureHandler, String... featureNames) {
        List<String> disabledFeatures = List.of(featureNames);
        try {
            Field featureInstancesField = featureHandler.getClass().getDeclaredField("featureInstances");
            featureInstancesField.setAccessible(true);
            List<Feature> allFeatures = (List<Feature>) featureInstancesField.get(featureHandler);
            for (int i = 0; i < allFeatures.size(); i++) {
                Feature featureInstance = allFeatures.get(i);
                if (disabledFeatures.stream().anyMatch(f -> f.equals(featureInstance.getClass().getName()))) {
                    allFeatures.set(i, DUMMY_FEATURE);
                }
            }
        } catch (ReflectiveOperationException e) {
            AnalysisError.shouldNotReachHere("Can't disable features.", e);
        }
    }

    public static void replaceImageSingletonEntry(Class<?> key, Object newValue) {
        try {
            Map<Class<?>, Object> configObjects = (Map<Class<?>, Object>) configObjectsField.get(ImageSingletonsSupportImpl.HostedManagement.get());
            configObjects.put(key, newValue);
        } catch (ReflectiveOperationException e) {
            VMError.shouldNotReachHere(e);
        }
    }
}
