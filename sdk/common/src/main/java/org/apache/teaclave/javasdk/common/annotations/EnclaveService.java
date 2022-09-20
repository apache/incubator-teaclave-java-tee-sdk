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

package org.apache.teaclave.javasdk.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark an interface is used as SPI service for Enclave.
 * All its providers' public non-static methods are {@link EnclaveMethod}s.
 * When the TEE side is native image (SVM), {@link EnclaveMethod}'s parameter and returned types may be automatically
 * registered for reflection and serialization at native image build time. But a class can be automatically registered
 * for serialization only when it:
 * <ul>
 * <li>Doesn't have {@code writeObject} method. The {@code writeObject} customizes the serialization rule, preventing
 * native image generator automatically inferring the associated serialization types. A typical example is {@link java.util.ArrayList}. </li>
 * <li>Is effective final, i.e. doesn't have subclasses.</li>
 * </ul>
 * Native image generator issues a warning when the parameter and returned value type don't obey the above two rules.
 * To solve the problem, user can choose another class for replacement if possible, e.g. using array instead of {@link java.util.ArrayList};
 * or generating the serialization configuration by <a href="https://github.com/ziyilin/ziyi-forked-graal/blob/master/docs/reference-manual/native-image/Agent.md">native-image-agent</a>.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnclaveService {
}
