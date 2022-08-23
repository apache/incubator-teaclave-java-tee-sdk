package com.alibaba.confidentialcomputing.enclave.agent;

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
