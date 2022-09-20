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

package org.apache.teaclave.javasdk.host.exception;

/**
 * EnclaveNativeInvokingException defines all kinds of possible exceptions towards an
 * enclave's native invocation. Basically there are two kinds error about enclave invocation,
 * one kind is native calling return an unexpected value, the other kind is an exception
 * happen in enclave and transform into host side. If a native invoking into enclave returns
 * an error value, enum of EnclaveNativeInvokingException will add extra error message details
 * for debugging; If an exception happened in enclave and transformed to host side, it will be
 * thrown again in host side to user.
 * Programmers need to handle EnclaveNativeInvokingException seriously.
 */
enum EnclaveNativeInvokingException {
    // Enclave creating failed.
    ENCLAVE_CREATING_ERROR("A0001", "creating enclave failed."),
    // Enclave destroying failed.
    ENCLAVE_DESTROYING_ERROR("A0002", "destroying enclave failed"),
    // Services loading failed.
    SERVICES_LOADING_ERROR("A0003", "services loading failed in enclave"),
    // Services unloading failed.
    SERVICES_UNLOADING_ERROR("A0004", "service unloading failed in enclave"),
    // Service method invoking failed.
    SERVICE_METHOD_INVOKING_ERROR("A0005", "service method invoking failed"),
    // Enclave remote attestation exception.
    ENCLAVE_REMOTE_ATTESTATION_ERROR("A0006", "tee remote attestation failed");

    private final String errorCode;
    private final String errorMessage;

    EnclaveNativeInvokingException(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    String buildExceptionMessage(String details) {
        if (details != null) {
            return this.toString() + " DetailErrorMessage: " + details;
        } else {
            return this.toString();
        }
    }

    @Override
    public String toString() {
        return "ErrorCode: " + this.errorCode + " , " + " ErrorMessage: " + this.errorMessage;
    }
}