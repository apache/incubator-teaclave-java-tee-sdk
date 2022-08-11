package com.alibaba.confidentialcomputing.benchmark.string.common;

import com.alibaba.confidentialcomputing.common.annotations.EnclaveService;

@EnclaveService
public interface StringOperationMetric {
    boolean stringRegex(String source, String pattern, int weight);
    String stringConcat(String source, String split, int weight);
    String[] stringSplit(String source, String concat, int weight);
}
