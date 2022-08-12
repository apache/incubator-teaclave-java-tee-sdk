package com.alibaba.confidentialcomputing.benchmark.string.common;

import com.alibaba.confidentialcomputing.common.annotations.EnclaveService;

@EnclaveService
public interface StringOperationMetric {
    boolean stringRegex(String source, String pattern, int iterator);
    String stringConcat(String source, String split, int iterator);
    String[] stringSplit(String source, String concat, int iterator);
}
