package com.alibaba.confidentialcomputing.test.enclave;

import com.alibaba.confidentialcomputing.test.common.EnclaveServiceStatistic;
import com.google.auto.service.AutoService;

import java.lang.reflect.Method;

@AutoService(EnclaveServiceStatistic.class)
public class EnclaveServiceStatisticImpl implements EnclaveServiceStatistic {
    @Override
    public int getEnclaveServiceCount() throws Exception {
        Method getInstance = Class.forName("com.alibaba.confidentialcomputing.enclave.framework.EnclaveContext").getMethod("getInstance");
        getInstance.setAccessible(true);
        Method servicesSize = Class.forName("com.alibaba.confidentialcomputing.enclave.framework.EnclaveContext").getMethod("servicesSize");
        servicesSize.setAccessible(true);
        Object enclaveContext = getInstance.invoke(null);
        return (int) servicesSize.invoke(enclaveContext);
    }
}
