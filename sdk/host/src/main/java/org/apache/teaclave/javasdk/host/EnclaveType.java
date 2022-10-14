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

/**
 * An enumeration of enclave type.
 * Teaclave Java TEE SDK supports three kinds of enclave, they are mock_in_jvm、mock_in_svm、tee_sdk
 * and embedded_lib_os.
 */
public enum EnclaveType {
    NONE,
    /**
     * A mock enclave environment, both host and enclave application run in the same
     * jvm environment, enclave services were discovered and loaded by SPI in host.
     */
    MOCK_IN_JVM,
    /**
     * A mock enclave environment, enclave application was compiled to machine code
     * by graalvm svm compiler, host application runs in jvm environment, and enclave
     * package was loaded by host.
     */
    MOCK_IN_SVM,
    /**
     * An enclave based on Intel's SGX2, with Intel's sgx sdk. Enclave application
     * was compiled to machine code and lint together with TEESdk's underlying libs,
     * host application runs in jvm environment, and enclave package were loaded by host.
     */
    TEE_SDK,
    /**
     * An enclave based on Intel's SGX2, with OCCLUM Libos. Enclave application
     * was compiled to .class files and packaged as a jar file, there is a jvm runs based
     * on enclave's occlum libos. host application runs in jvm environment, and enclave
     * package were loaded by host.
     */
    EMBEDDED_LIB_OS;

    public String getEnclaveType() {
        return this.toString();
    }
}