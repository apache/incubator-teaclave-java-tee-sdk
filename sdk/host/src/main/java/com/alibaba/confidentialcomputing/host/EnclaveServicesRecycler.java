package com.alibaba.confidentialcomputing.host;

import com.alibaba.confidentialcomputing.host.exception.ServicesUnloadingException;

import java.lang.ref.Cleaner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * EnclaveServicesRecycler is responsible for an enclave's services resource recycling.
 * If a service handler in host side was recycled by gc, EnclaveServicesRecycle will help
 * to recycle the corresponding service loaded in enclave side.
 * EnclaveServicesRecycle starts a new thread to recycle enclave's services asynchronously.
 */
class EnclaveServicesRecycler extends BaseEnclaveServicesRecycler {
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
    void enqueueProxyHandler(ProxyEnclaveInvocationHandler handler) {
        try {
            toBeReleasedEnclaveServices.add(handler);
        } catch (IllegalStateException | ClassCastException | NullPointerException | IllegalArgumentException e) {
            // Have to handle this exception locally.
            e.printStackTrace();
        }
    }

    // register service's proxy handler when it's created.
    @Override
    void registerProxyHandler(ProxyEnclaveInvocationHandler handler) {
        cleaner.register(handler, handler);
    }

    // interrupt enclave services' recycler thread exit.
    @Override
    void interruptServiceRecycler() {
        recyclerThread.interrupt();
    }
}
