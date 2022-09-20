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
 * ServicesLoadingException {@link ServicesLoadingException} is thrown when exception happen
 * during an enclave's service was loading.
 * Programmers need to handle ServicesLoadingException seriously.
 */
public class ServicesLoadingException extends ConfidentialComputingException {
    /**
     * @param info exception information.
     */
    public ServicesLoadingException(String info) {
        super(EnclaveNativeInvokingException.SERVICES_LOADING_ERROR.buildExceptionMessage(info));
    }

    /**
     * @param e exception.
     */
    public ServicesLoadingException(Throwable e) {
        super(EnclaveNativeInvokingException.SERVICES_LOADING_ERROR.toString(), e);
    }

    /**
     * @param info exception info.
     * @param e    exception.
     */
    public ServicesLoadingException(String info, Throwable e) {
        super(EnclaveNativeInvokingException.SERVICES_LOADING_ERROR.buildExceptionMessage(info), e);
    }
}