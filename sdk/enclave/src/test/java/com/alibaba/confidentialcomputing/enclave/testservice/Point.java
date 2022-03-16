package com.alibaba.confidentialcomputing.enclave.testservice;

import java.io.Serializable;

public class Point implements Serializable {

    private static final long serialVersionUID = -3715916707782706029L;

    public int x;
    public int y;

    public Point(int x, int y){
        this.x = x;
        this.y = y;
    }
}
