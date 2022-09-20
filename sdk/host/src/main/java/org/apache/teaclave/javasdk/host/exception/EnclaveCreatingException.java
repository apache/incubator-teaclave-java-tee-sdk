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

import org.apache.teaclave.javasdk.common.exception.ConfidentialComputingException;

/**
 * EnclaveCreatingException {@link EnclaveCreatingException} is thrown when exception happen
 * during an enclave was creating.
 * Programmers need to handle EnclaveCreatingException seriously.
 */
public class EnclaveCreatingException extends ConfidentialComputingException {

    /**
     * @param info exception information.
     */
    public EnclaveCreatingException(String info) {
        super(EnclaveNativeInvokingException.ENCLAVE_CREATING_ERROR.buildExceptionMessage(info));
    }

    /**
     * @param e exception.
     */
    public EnclaveCreatingException(Throwable e) {
        super(EnclaveNativeInvokingException.ENCLAVE_CREATING_ERROR.toString(), e);
    }

    /**
     * @param info exception message.
     * @param e    exception.
     */
    public EnclaveCreatingException(String info, Throwable e) {
        super(EnclaveNativeInvokingException.ENCLAVE_CREATING_ERROR.buildExceptionMessage(info), e);
    }
}