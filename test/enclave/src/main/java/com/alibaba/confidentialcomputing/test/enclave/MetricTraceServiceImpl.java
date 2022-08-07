package com.alibaba.confidentialcomputing.test.enclave;

import com.alibaba.confidentialcomputing.test.common.MetricTraceService;
import com.google.auto.service.AutoService;

@AutoService(MetricTraceService.class)
public class MetricTraceServiceImpl implements MetricTraceService {
    @Override
    public String invertCharacter(String str) {
        byte[] content = new byte[str.length()];
        byte[] initial = str.getBytes();
        for (int i = 0x0; i < initial.length; i++) {
            content[i] = initial[initial.length - i -1];
        }
        return new String(content);
    }
}
