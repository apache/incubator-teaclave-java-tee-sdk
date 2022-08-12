package com.alibaba.confidentialcomputing.benchmark.string.enclave;

import com.alibaba.confidentialcomputing.benchmark.string.common.StringOperationMetric;
import com.google.auto.service.AutoService;

import java.util.regex.Pattern;

@AutoService(StringOperationMetric.class)
public class StringOperationMetricImpl implements StringOperationMetric {
    private final long INNER_MAX_ITERATOR = 50;

    @Override
    public String stringConcat(String source, String concat, int iterator) {
        String result = null;
        for (int i = 0x0; i < iterator; i++) {
            for (int j = 0x0; j < INNER_MAX_ITERATOR; j++) {
                result = source.concat(concat).toLowerCase().trim().replace('a', 'b');
            }
        }
        return result;
    }

    @Override
    public boolean stringRegex(String source, String pattern, int iterator) {
        boolean matched = false;
        for (int i = 0x0; i < iterator; i++) {
            for (int j = 0x0; j < INNER_MAX_ITERATOR; j++) {
                Pattern p = Pattern.compile(pattern);
                matched = p.matcher(source).matches();
            }
        }
        return matched;
    }

    @Override
    public String[] stringSplit(String source, String split, int iterator) {
        String[] result = null;
        for (int i = 0x0; i < iterator; i++) {
            for (int j = 0x0; j < INNER_MAX_ITERATOR; j++) {
                result = source.split(split);
            }
        }
        return result;
    }
}
