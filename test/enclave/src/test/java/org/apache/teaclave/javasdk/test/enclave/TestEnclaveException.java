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

package org.apache.teaclave.javasdk.test.enclave;

import org.apache.teaclave.javasdk.test.common.JavaEnclaveException;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestEnclaveException {
    @Test
    public void testEnclaveException() {
        assertThrows(JavaEnclaveException.class, () -> new EnclaveExceptionImpl().enclaveException("Teaclave-java-tee-sdk Exception"));
    }
}
