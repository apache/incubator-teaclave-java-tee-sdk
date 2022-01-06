package com.alibaba.confidentialcomputing.host;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * EnclaveToken avoids an enclave's invocation happen when it's being destroyed.
 */
class EnclaveToken {
    private volatile AtomicBoolean alive = new AtomicBoolean(true);
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
                ; // Should never happen, do nothing here.
            }
            return true;
        }
        return false;
    }
}
