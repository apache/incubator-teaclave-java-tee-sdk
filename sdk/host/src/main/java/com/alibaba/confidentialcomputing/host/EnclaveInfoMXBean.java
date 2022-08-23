package com.alibaba.confidentialcomputing.host;

import java.util.List;

/**
 * EnclaveInfoMXBean help query created all existed enclave's detail information.
 * Such as existed enclave instance number, every enclave's type info, is debuggable,
 * and enclave's epc memory size.
 */
public interface EnclaveInfoMXBean {
    /**
     * get the number of all existed enclaves.
     *
     * @return number of all existed enclaves.
     */
    int getEnclaveInstanceNumber();

    /**
     * get all existed enclaves' EnclaveInfo details.
     *
     * @return List<EnclaveInfo> all existed enclaves' EnclaveInfo details.
     */
    List<EnclaveInfo> getEnclaveInstancesInfo();
}
