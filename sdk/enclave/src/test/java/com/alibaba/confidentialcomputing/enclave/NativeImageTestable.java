package com.alibaba.confidentialcomputing.enclave;

import java.util.List;

public interface NativeImageTestable {
    default void runWithNativeImageAgent(){}
    default void beforeSVMCompile(){}
    default void afterSVMCompile(){}
    default List<String> extraSVMOptions() {
        return null;
    }
}
