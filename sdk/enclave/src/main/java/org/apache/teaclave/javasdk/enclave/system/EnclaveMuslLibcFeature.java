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

import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.core.c.libc.LibCBase;
import com.oracle.svm.core.posix.linux.libc.LibCFeature;
import com.oracle.svm.core.posix.linux.libc.MuslLibC;
import com.oracle.svm.core.util.UserError;
import org.graalvm.compiler.serviceprovider.JavaVersionUtil;
import org.graalvm.nativeimage.ImageSingletons;

import java.util.ServiceLoader;

@AutomaticFeature
public class EnclaveMuslLibcFeature extends LibCFeature {

    @Override
    public void afterRegistration(AfterRegistrationAccess access) {
        String targetLibC = LibCOptions.UseLibC.getValue();
        ServiceLoader<LibCBase> loader = ServiceLoader.load(LibCBase.class);
        for (LibCBase libc : loader) {
            if (libc.getName().equals(targetLibC)) {
                if (libc.getName().equals(MuslLibC.NAME)) {
                    if (JavaVersionUtil.JAVA_SPEC < 11) {
                        throw UserError.abort("Musl can only be used with labsjdk 11+.");
                    }
                } else {
                    libc.checkIfLibCSupported();
                }
                ImageSingletons.add(LibCBase.class, libc);
                return;
            }
        }
        throw UserError.abort("Unknown libc %s selected. Please use one of the available libc implementations.", targetLibC);
    }
}
