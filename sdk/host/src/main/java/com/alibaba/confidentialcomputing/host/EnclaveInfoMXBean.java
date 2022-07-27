package com.alibaba.confidentialcomputing.host;

import java.util.List;

/**
 * EnclaveInfoMXBean help query created all existed enclave's detail information.
 * Such as existed enclave instance number, every enclave's type info, is debuggable,
 * and enclave's epc memory size and so on.
 */
public interface EnclaveInfoMXBean {
    /**
     * get all existed enclaves' number.
     *
     * @return int existed enclaves' number.
     */
    int getEnclaveInstanceNumber();

    /**
     * get all existed enclaves' EnclaveInfo details.
     *
     * @return List<EnclaveInfo> all existed enclaves' EnclaveInfo details.
     */
    List<EnclaveInfo> getEnclaveInstancesInfo();
}
