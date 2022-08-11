package com.alibaba.confidentialcomputing.benchmark.string.enclave;

import com.alibaba.confidentialcomputing.benchmark.string.common.StringOperationMetric;
import com.google.auto.service.AutoService;

import java.util.regex.Pattern;

@AutoService(StringOperationMetric.class)
public class StringOperationMetricImpl implements StringOperationMetric {

    @Override
    public String stringConcat(String source, String concat, int weight) {
        String result = null;
        for (int i = 0x0; i < weight; i++) {
            result = source.concat(concat).toLowerCase().trim().replace('a', 'b');
        }
        return result;
    }

    @Override
    public boolean stringRegex(String source, String pattern, int weight) {
        boolean matched = false;
        for (int i = 0x0; i < weight; i++) {
            Pattern p = Pattern.compile(pattern);
            matched = p.matcher(source).matches();
        }
        return matched;
    }

    @Override
    public String[] stringSplit(String source, String split, int weight) {
        String[] result = null;
        for (int i = 0x0; i < weight; i++) {
            result = source.split(split);
        }
        return result;
    }
}
