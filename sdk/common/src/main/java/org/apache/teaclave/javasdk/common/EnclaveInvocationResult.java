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
 * EnclaveInvocationResult is a service's method invoking return value in an enclave.
 * If an exception happened during the invocation, the exception will be stored in the
 * EnclaveInvocationResult object's exception field, the result field will be null.
 * If no exception happened during the invocation, the method invoking return value is
 * stored in the EnclaveInvocationResult object's result field, and the exception field
 * will be null.
 */
public final class EnclaveInvocationResult implements Serializable {
    private static final long serialVersionUID = -571664787738930979L;

    private final Object resultedValue;
    private final Throwable exception;
    private long cost; // ns.

    public EnclaveInvocationResult(Object result, Throwable exception) {
        this.resultedValue = result;
        this.exception = exception;
    }

    /**
     * get method's return value.
     *
     * @return method's return value.
     */
    public Object getResult() {
        return this.resultedValue;
    }

    /**
     * get exception during method's invocation.
     *
     * @return exception during method's invocation if it has.
     */
    public Throwable getException() {
        return this.exception;
    }

    /**
     * set method's overhead.
     *
     */
    public void setCost(long cost) {
        this.cost = cost;
    }

    /**
     * get method's overhead.
     *
     * @return method's overhead(ns).
     */
    public long getCost() {
        return this.cost;
    }
}