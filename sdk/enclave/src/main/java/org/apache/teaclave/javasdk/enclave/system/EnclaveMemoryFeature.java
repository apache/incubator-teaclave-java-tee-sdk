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

package org.apache.teaclave.javasdk.enclave.system;

import org.apache.teaclave.javasdk.enclave.EnclaveOptions;
import org.apache.teaclave.javasdk.enclave.EnclavePlatFormSettings;
import org.apache.teaclave.javasdk.enclave.c.EnclaveEnvironment;
import org.apache.teaclave.javasdk.enclave.system.EnclavePhysicalMemory.PhysicalMemorySupportImpl;
import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.core.os.VirtualMemoryProvider;
import com.oracle.svm.core.util.VMError;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.impl.RuntimeClassInitializationSupport;

import java.util.List;

/**
 * Native image queries the memory page size and heap pages number at runtime with {@code sysconf(_SC_PHYS_PAGES)} and
 * {@code sysconf(_SC_PAGESIZE)}, just as POSIX defined. However, such operations are not supported by some enclave SDKs,
 * such as OE and TEE.
 * We define three custom native methods to get the corresponding memory information: {@link EnclaveEnvironment#getPhysicalPageNumber()},
 * {@link EnclaveEnvironment#getPhysicalPageSize()} and {@link EnclaveEnvironment#getVirtualPageSize()}. They should be
 * implemented in native code and linked by out framework. See {@code test/resources/native/enc_invoke_entry_test.c} and
 * {@code org.apache.teaclave.javasdk.enclave.NativeImageTest#compileJNILibrary()} for details.
 * <p>
 */
@AutomaticFeature
public class EnclaveMemoryFeature implements Feature {
    @Override
    public List<Class<? extends Feature>> getRequiredFeatures() {
        try {
            Class<? extends Feature> physicalMemClass = (Class<? extends Feature>) Class.forName("com.oracle.svm.core.posix.linux.LinuxPhysicalMemory$PhysicalMemoryFeature");
            return List.of(physicalMemClass);
        } catch (ClassNotFoundException e) {
            throw VMError.shouldNotReachHere(e);
        }
    }

    @Override
    public void afterRegistration(AfterRegistrationAccess access) {
        if (EnclaveOptions.RunInEnclave.getValue()) {
            RuntimeClassInitializationSupport rci = ImageSingletons.lookup(RuntimeClassInitializationSupport.class);
            rci.initializeAtBuildTime("org.apache.teaclave.javasdk.enclave.system.EnclaveVirtualMemoryProvider", "Native Image classes are always initialized at build time");
            EnclavePlatFormSettings.replaceImageSingletonEntry(PhysicalMemorySupportImpl.getPhysicalMemorySupportClass(), new PhysicalMemorySupportImpl());
            ImageSingletons.add(VirtualMemoryProvider.class, new EnclaveVirtualMemoryProvider());
        }
    }
}
