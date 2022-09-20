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

package org.apache.teaclave.javasdk.common;

import java.io.Serializable;

/**
 * This class is used to transfer embedded lib os attestation report between Teaclave-java-tee-sdk's
 * host and enclave module.
 */
public final class EmbeddedLibOSInnerAttestationReport implements Serializable {
    private static final long serialVersionUID = -6944029051086666440L;

    private final byte[] quote;
    private final byte[] mrSigner;
    private final byte[] mrEnclave;
    private final byte[] userData;


    public EmbeddedLibOSInnerAttestationReport(byte[] quote, byte[] mrSigner, byte[] mrEnclave, byte[] userData) {
        this.quote = quote;
        this.mrSigner = mrSigner;
        this.mrEnclave = mrEnclave;
        this.userData = userData;
    }

    public byte[] getQuote() {
        return this.quote;
    }

    public byte[] getMrSigner() {
        return this.mrSigner;
    }

    public byte[] getMrEnclave() {
        return this.mrEnclave;
    }

    public byte[] getUserData() {
        return this.userData;
    }
}
