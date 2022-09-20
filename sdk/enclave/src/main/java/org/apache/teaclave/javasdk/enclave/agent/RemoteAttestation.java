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

package org.apache.teaclave.javasdk.enclave.agent;

import org.apache.teaclave.javasdk.common.EmbeddedLibOSInnerAttestationReport;
import org.apache.teaclave.javasdk.common.exception.ConfidentialComputingException;

public class RemoteAttestation {
    // lib os embedded enclave remote attestation jni.so path in occlum image.
    private final static String JNI_EXTRACTED_PACKAGE_PATH = "/usr/lib/libos_occlum_enclave_attestation/lib_occlum_attestation_generate.so";

    private static native void registerNatives();
    private static native EmbeddedLibOSInnerAttestationReport generateAttestationReportNative(byte[] userDate) throws ConfidentialComputingException;

    static {
        System.load(JNI_EXTRACTED_PACKAGE_PATH);
        registerNatives();
    }

    public static EmbeddedLibOSInnerAttestationReport generateAttestationReport(byte[] userDate) throws ConfidentialComputingException {
        return generateAttestationReportNative(userDate);
    }
}
