// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.teaclave.javasdk.enclave;

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
