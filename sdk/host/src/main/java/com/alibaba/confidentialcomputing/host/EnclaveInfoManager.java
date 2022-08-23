package com.alibaba.confidentialcomputing.host;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * EnclaveInfoManager stores all existed enclave's detail information. Such as the number of
 * all existed enclave instance, every enclave's type info, debuggable or not, and enclave's
 * epc memory size.
 */
public final class EnclaveInfoManager implements EnclaveInfoMXBean {
    private final static EnclaveInfoManager instance = new EnclaveInfoManager();
    private final HashMap<Enclave, Object> enclaveRecord = new HashMap<>();

    /**
     * get a single instance of EnclaveInfoManager.
     *
     * @return a single instance of EnclaveInfoManager.
     */
    public static EnclaveInfoManager getEnclaveInfoManagerInstance() {
        return instance;
    }

    synchronized void addEnclave(Enclave enclave) {
        enclaveRecord.put(enclave, null);
    }

    synchronized void removeEnclave(Enclave enclave) {
        enclaveRecord.remove(enclave);
    }

    /**
     * number of all existed enclaves.
     *
     * @return number of existed enclaves.
     */
    @Override
    public synchronized int getEnclaveInstanceNumber() {
        return enclaveRecord.size();
    }

    /**
     * get all existed enclaves' EnclaveInfo details.
     *
     * @return List<EnclaveInfo> all existed enclaves' EnclaveInfo details.
     */
    @Override
    public synchronized List<EnclaveInfo> getEnclaveInstancesInfo() {
        List<EnclaveInfo> enclaveInfos = new ArrayList<>();
        for (Enclave enclave : enclaveRecord.keySet()) {
            enclaveInfos.add(enclave.getEnclaveInfo());
        }
        return enclaveInfos;
    }
}
