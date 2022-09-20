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

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * EnclaveToken avoids an enclave's invocation happen when it's being destroyed.
 */
final class EnclaveToken {
    private final AtomicBoolean alive = new AtomicBoolean(true);
    private final int MAX_CONCURRENCY_INVOKER = 999999;
    private final Semaphore tokens = new Semaphore(MAX_CONCURRENCY_INVOKER);

    /**
     * tryAcquireToken try to get an enclave's token.
     */
    boolean tryAcquireToken() {
        if (alive.get()) {
            return tokens.tryAcquire();
        }
        return false;
    }

    /**
     * restoreToken restores the enclave token.
     */
    void restoreToken() {
        tokens.release();
    }

    /**
     * destroyToken prevents an enclave invocation and waits for all
     * ongoing enclave invocations finished.
     */
    boolean destroyToken() {
        if (alive.compareAndSet(true, false)) {
            try {
                tokens.acquire(MAX_CONCURRENCY_INVOKER);
            } catch (InterruptedException e) {
                // Should never happen, do nothing here.
            }
            return true;
        }
        return false;
    }
}
