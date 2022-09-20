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

package org.apache.teaclave.javasdk.host;

import org.apache.teaclave.javasdk.host.exception.RemoteAttestationException;

import java.io.IOException;

final class SGXRemoteAttestationVerify {
    private final static String JNI_EXTRACTED_PACKAGE_PATH = "remote_attestation/sgx/jni/lib_jni_sgx_remote_attestation_verify.so";

    static {
        try {
            String jniTempFilePath = ExtractLibrary.extractLibrary(SGXRemoteAttestationVerify.class.getClassLoader(),
                    JNI_EXTRACTED_PACKAGE_PATH);
            System.load(jniTempFilePath);
            registerNatives();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static native void registerNatives();
    private static native int nativeVerifyAttestationReport(byte[] report, RemoteAttestationVerifyResult result);

    static int VerifyAttestationReport(byte[] report) throws RemoteAttestationException {
        RemoteAttestationVerifyResult verifyResult = new RemoteAttestationVerifyResult(0, 0, 0);
        nativeVerifyAttestationReport(report, verifyResult);
        if (verifyResult.getVersionCheck() == -1) {
            throw new RemoteAttestationException("sgx_qv_get_quote_supplemental_data_size returned size is not same with header definition in SGX SDK");
        } else if (verifyResult.getStatus() == 1) {
            throw new RemoteAttestationException("sgx_qv_get_quote_supplemental_data_size failed");
        } else if (verifyResult.getStatus() == 2) {
            throw new RemoteAttestationException("sgx_qv_verify_quote failed");
        } else if (verifyResult.getStatus() == 3) {
            throw new RemoteAttestationException("supplemental data memory allocation failed");
        } else if (verifyResult.getVerifyFlag() == 1) {
            throw new RemoteAttestationException("verification completed, but collateral is out of date");
        } else if (verifyResult.getVerifyFlag() == 2) {
            throw new RemoteAttestationException("verification completed with non-terminal result");
        } else if (verifyResult.getVerifyFlag() == 3) {
            throw new RemoteAttestationException("verification completed with terminal result, but verification check failed");
        } else {
            return verifyResult.getVerifyFlag();
        }
    }
}
