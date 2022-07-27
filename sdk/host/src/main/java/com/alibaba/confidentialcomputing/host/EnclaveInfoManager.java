package com.alibaba.confidentialcomputing.host;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EnclaveInfoManager implements EnclaveInfoMXBean {
    private final static EnclaveInfoManager instance = new EnclaveInfoManager();
    private HashMap<Enclave, Object> enclaveRecord = new HashMap<>();

    public static EnclaveInfoManager getEnclaveInfoManagerInstance() {
        return instance;
    }

    synchronized void addEnclave(Enclave enclave) {
        enclaveRecord.put(enclave, null);
    }

    synchronized void removeEnclave(Enclave enclave) {
        enclaveRecord.remove(enclave);
    }

    @Override
    public synchronized int getEnclaveInstanceNumber() {
        return enclaveRecord.size();
    }

    @Override
    public synchronized List<EnclaveInfo> getEnclaveInstancesInfo() {
        List<EnclaveInfo> enclaveInfos = new ArrayList<>();
        for (Enclave enclave : enclaveRecord.keySet()) {
            enclaveInfos.add(enclave.getEnclaveInfo());
        }
        return enclaveInfos;
    }
}
