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
 * EnclaveDestroyingException {@link EnclaveDestroyingException} is thrown when exception happen
 * during an enclave was destroying.
 * Programmers need to handle EnclaveDestroyingException seriously.
 */
public class EnclaveDestroyingException extends ConfidentialComputingException {
    /**
     * @param info exception information.
     */
    public EnclaveDestroyingException(String info) {
        super(EnclaveNativeInvokingException.ENCLAVE_DESTROYING_ERROR.buildExceptionMessage(info));
    }

    /**
     * @param e exception.
     */
    public EnclaveDestroyingException(Throwable e) {
        super(EnclaveNativeInvokingException.ENCLAVE_DESTROYING_ERROR.toString(), e);
    }

    /**
     * @param info exception message.
     * @param e    exception.
     */
    public EnclaveDestroyingException(String info, Throwable e) {
        super(EnclaveNativeInvokingException.ENCLAVE_DESTROYING_ERROR.buildExceptionMessage(info), e);
    }
}