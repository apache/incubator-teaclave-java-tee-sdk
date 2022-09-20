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

package org.apache.teaclave.javasdk.enclave.testservice;

import org.apache.teaclave.javasdk.common.annotations.EnclaveService;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@EnclaveService
public interface MathService<T> {
    T add(T x, T y);

    T minus(T x, T y);

    T div(T x, T y);

    default int getConstant() {
        return 100;
    }

    default byte[] getRandomNumber(int size) {
        SecureRandom secureRandom = null;
        try {
            secureRandom = SecureRandom.getInstance("NativePRNG");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return secureRandom.generateSeed(size);
    }
}
