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

package org.apache.teaclave.javasdk.common.exception;

/**
 * ConfidentialComputingException {@link ConfidentialComputingException} is base exception in
 * Teaclave-java-tee-sdk. All exceptions thrown in Teaclave-java-tee-sdk will inherit this base exception.
 * Programmers need to handle ConfidentialComputingException seriously.
 */
public class ConfidentialComputingException extends Exception {

    private static final long serialVersionUID = 5964126736764332957L;

    public ConfidentialComputingException() {super();}

    /**
     * @param info exception information.
     */
    public ConfidentialComputingException(String info) {
        super(info);
    }

    /**
     * @param e exception.
     */
    public ConfidentialComputingException(Throwable e) {
        super(e);
    }

    /**
     * @param info exception information.
     * @param e    exception.
     */
    public ConfidentialComputingException(String info, Throwable e) {
        super(info, e);
    }
}