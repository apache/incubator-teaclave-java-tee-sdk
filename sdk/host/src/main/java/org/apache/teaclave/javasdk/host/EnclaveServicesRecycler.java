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

import org.apache.teaclave.javasdk.host.exception.ServicesUnloadingException;

import java.lang.ref.Cleaner;
import java.lang.reflect.InvocationHandler;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * EnclaveServicesRecycler is responsible for an enclave's services resource recycling.
 * If a service handler in host side was recycled by gc, EnclaveServicesRecycle will help
 * to recycle the corresponding service loaded in enclave side.
 * EnclaveServicesRecycle starts a new thread to recycle enclave's services asynchronously.
 */
final class EnclaveServicesRecycler extends BaseEnclaveServicesRecycler {
    private final Cleaner cleaner = Cleaner.create();
    // toBeReleasedEnclaveServices stores a service proxy handler when it's recycled
    // by gc in host side.
    private final BlockingQueue<ProxyEnclaveInvocationHandler> toBeReleasedEnclaveServices = new LinkedBlockingQueue<>();
    private final Thread recyclerThread;

    EnclaveServicesRecycler() {
        recyclerThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    ProxyEnclaveInvocationHandler proxyHandler = toBeReleasedEnclaveServices.take();
                    proxyHandler.getEnclave().unloadService(proxyHandler.getServiceHandler());
                } catch (InterruptedException e) {
                    break; // Recycle Thread should exit when enclave destroyed.
                } catch (ServicesUnloadingException e) {
                    // Have to handle this exception locally, print to log later.
                    e.printStackTrace();
                }
            }
        });
        recyclerThread.setDaemon(true);
        recyclerThread.start();
    }

    // enqueue the recycled proxy handler object of a service handler.
    @Override
    void enqueueProxyHandler(InvocationHandler handler) {
        try {
            toBeReleasedEnclaveServices.add((ProxyEnclaveInvocationHandler) handler);
        } catch (IllegalStateException | ClassCastException | NullPointerException | IllegalArgumentException e) {
            // Have to handle this exception locally.
            e.printStackTrace();
        }
    }

    // register service's proxy handler when it's created.
    @Override
    void registerProxyHandler(Object obj, InvocationHandler handler) {
        cleaner.register(obj, (ProxyEnclaveInvocationHandler)handler);
    }

    // interrupt enclave services' recycler thread exit.
    @Override
    void interruptServiceRecycler() {
        recyclerThread.interrupt();
    }
}
