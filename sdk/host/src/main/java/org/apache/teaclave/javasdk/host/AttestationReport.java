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

import java.io.Serializable;

/**
 * AttestationReport includes an enclave's type and generated remote attestation report.
 */
public class AttestationReport implements Serializable {
    private static final long serialVersionUID = -2781780414647128479L;

    private final EnclaveType enclaveType;
    private final byte[] quote;

    AttestationReport(EnclaveType enclaveType, byte[] quote) {
        this.enclaveType = enclaveType;
        this.quote = quote;
    }

    /**
     * Get enclave type from an AttestationReport instance.
     * <p>
     *
     * @return Enclave type.
     */
    public EnclaveType getEnclaveType() {
        return enclaveType;
    }

    /**
     * Get enclave quote from an AttestationReport instance.
     * <p>
     *
     * @return Remote attestation quote data.
     */
    public byte[] getQuote() {
        return quote;
    }

    /**
     * Bind an AttestationReport's type and quote into a buffer for rpc transmission.
     * <p>
     *
     * @return Serialized buffer.
     */
    public byte[] toByteArray() {
        byte[] bindReport = new byte[1 + quote.length];
        bindReport[0] = (byte) enclaveType.ordinal();
        System.arraycopy(quote, 0, bindReport, 1, quote.length);
        return bindReport;
    }

    /**
     * Build an AttestationReport instance from a bind buffer which contains its type and report.
     * <p>
     *
     * @return AttestationReport instance.
     */
    public static AttestationReport fromByteArray(byte[] attestationReport) {
        EnclaveType enclaveType = EnclaveType.NONE;
        byte[] report = new byte[attestationReport.length - 1];
        switch (attestationReport[0]) {
            case 0:
                break;
            case 1:
                enclaveType = EnclaveType.MOCK_IN_JVM;
                break;
            case 2:
                enclaveType = EnclaveType.MOCK_IN_SVM;
                break;
            case 3:
                enclaveType = EnclaveType.TEE_SDK;
                break;
            case 4:
                enclaveType = EnclaveType.EMBEDDED_LIB_OS;
                break;
        }
        System.arraycopy(attestationReport, 1, report, 0, report.length);
        return new AttestationReport(enclaveType, report);
    }
}
