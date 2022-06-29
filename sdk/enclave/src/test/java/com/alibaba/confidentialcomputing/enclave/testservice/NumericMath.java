package com.alibaba.confidentialcomputing.enclave.testservice;

public class NumericMath implements MathService<Number> {

    private int counter = 0;

    @Override
    public Number add(Number x, Number y) {
        synchronized (this) {
            counter++;
        }
        return x.intValue() + y.intValue();
    }

    @Override
    public Number minus(Number x, Number y) {
        synchronized (this) {
            counter++;
        }
        return x.intValue() - y.intValue();
    }

    @Override
    public Number div(Number x, Number y) {
        synchronized (this) {
            counter++;
        }
        return x.intValue() / y.intValue();
    }

    public int getCounter() {
        synchronized (this) {
            return counter;
        }
    }
}
