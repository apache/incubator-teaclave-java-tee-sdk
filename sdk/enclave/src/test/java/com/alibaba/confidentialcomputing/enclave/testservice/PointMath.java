package com.alibaba.confidentialcomputing.enclave.testservice;

public class PointMath implements MathService<Point>{
    @Override
    public Point add(Point x, Point y) {
        return new Point(x.x + y.x, x.y + y.y);
    }

    @Override
    public Point minus(Point x, Point y) {
        return new Point(x.x - y.x, x.y - y.y);
    }

    @Override
    public Point div(Point x, Point y) {
        return new Point(x.x / y.x, x.y / y.y);
    }
}
