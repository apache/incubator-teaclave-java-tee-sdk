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

package org.apache.teaclave.javasdk.enclave.agent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class EnclaveShutDown {
    private static final int SHUTDOWN_DELAY_DURATION = 1000; // TimeUnit.MilliSecond
    private static final BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();

    static void shutDownNotify() {
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    queue.put(0);
                    return;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static void shutDownWait() {
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    queue.take();
                    // wait for destroy localhost remote destroy invocation return.
                    Thread.sleep(SHUTDOWN_DELAY_DURATION);
                    // close cached socket resources and socket service.
                    EnclaveAgent.closeHttpService();
                    return;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
