package com.alibaba.confidentialcomputing.test.common;

import java.io.Serializable;

public class AESSealedTest implements Serializable, Comparable<AESSealedTest> {
    private String name;
    private int age;
    private int level;

    public AESSealedTest(String name, int age, int level) {
        this.name = name;
        this.age = age;
        this.level = level;
    }

    @Override
    public int compareTo(AESSealedTest aesSealedTest) {
        if (this.name.equals(aesSealedTest.name)
                && this.age == aesSealedTest.age
                && this.level == aesSealedTest.level) {
            return 0;
        }
        return -1;
    }
}
