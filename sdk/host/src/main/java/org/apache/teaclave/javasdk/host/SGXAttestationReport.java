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
 * SGX type enclave's remote attestation report.
 */
public class SGXAttestationReport extends AttestationReport {
    private final byte[] mrSigner;
    private final byte[] mrEnclave;
    private final byte[] userData;

    SGXAttestationReport(EnclaveType type, byte[] quote, byte[] mrSigner, byte[] mrEnclave, byte[] userData) {
        super(type, quote);
        this.mrSigner = mrSigner;
        this.mrEnclave = mrEnclave;
        this.userData = userData;
    }

    /**
     * Get enclave userData from an enclave's remote attestation report.
     * <p>
     *
     * @return Remote attestation userData value which is from user.
     */
    public byte[] getUserData() {
        return this.userData;
    }

    /**
     * Get enclave measurementEnclave from an enclave's remote attestation report.
     * <p>
     *
     * @return Remote attestation measurementEnclave value.
     */
    public byte[] getMeasurementEnclave() {
        return this.mrEnclave;
    }

    /**
     * Get enclave measurementSigner from an enclave's remote attestation report.
     * <p>
     *
     * @return Remote attestation measurementSigner value.
     */
    public byte[] getMeasurementSigner() {
        return this.mrSigner;
    }
}
