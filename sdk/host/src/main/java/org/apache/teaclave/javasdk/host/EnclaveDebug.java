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
 * An enumeration of enclave debug mode.
 */
enum EnclaveDebug {
    /**
     * For MOCK_IN_JVM and MOCK_IN_SVM, there is no real enclave environment.
     */
    NONE(0),
    /**
     * TEE_SDK could debug by gdb tool in this mode.
     */
    DEBUG(1),
    /**
     * TEE_SDK could not debug by gdb tool in this mode.
     */
    RELEASE(2);

    private final int value;

    EnclaveDebug(int value) {
        this.value = value;
    }

    int getValue() {
        return value;
    }
}
