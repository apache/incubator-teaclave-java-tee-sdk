package com.alibaba.confidentialcomputing.test.enclave;

import com.alibaba.confidentialcomputing.test.common.ConcurrencyCalculate;
import com.google.auto.service.AutoService;

import java.util.concurrent.atomic.AtomicLong;

@AutoService(ConcurrencyCalculate.class)
public class ConcurrencyCalculateImpl implements ConcurrencyCalculate {
    private AtomicLong sum = new AtomicLong();
    private long sumSync = 0x0;

    @Override
    public void add(int delta) {
        sum.addAndGet(delta);
    }

    @Override
    public long sum() {
        return sum.get();
    }

    @Override
    public synchronized void addSync(int delta) {
        while(delta > 0x0) {
            sumSync++;
            delta--;
        }
    }

    @Override
    public synchronized long sumSync() {
        return sumSync;
    }
}
