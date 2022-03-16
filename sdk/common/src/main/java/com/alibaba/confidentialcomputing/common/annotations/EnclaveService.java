/*
 * Copyright (c) 2021, 2021, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021, 2021, Alibaba Group Holding Limited. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.alibaba.confidentialcomputing.common.annotations;

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
