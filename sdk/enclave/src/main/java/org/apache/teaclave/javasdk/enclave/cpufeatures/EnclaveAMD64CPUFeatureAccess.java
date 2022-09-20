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

package org.apache.teaclave.javasdk.enclave.cpufeatures;

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
