package com.alibaba.confidentialcomputing.common;

import java.io.Serializable;

/**
 * ServiceHandler is a handler object in host to the real service loaded in the enclave.
 * A ServiceHandler object will be bound to a host proxy handler.
 */
public final class ServiceHandler implements Serializable {
    private static final long serialVersionUID = -879933256236932801L;

    // instanceIdentity indicates the global unique service object's index in one enclave.
    private final String instanceIdentity;
    // serviceImplClass stores loaded service object's full signature in the enclave.
    private final String serviceImplClass;
    // serviceInterface stores loaded service object's implement interface's full signature in the enclave.
    private final String serviceInterface;

    public ServiceHandler(String serviceInterfaceName, String serviceClassName, String instanceIdentity) {
        this.serviceInterface = serviceInterfaceName;
        this.serviceImplClass = serviceClassName;
        this.instanceIdentity = instanceIdentity;
    }

    public ServiceHandler(String interfaceName) {
        this.instanceIdentity = null;
        this.serviceImplClass = null;
        this.serviceInterface = interfaceName;
    }

    /**
     * get service's unique identity.
     *
     * @return service's unique identity.
     */
    public String getInstanceIdentity() {
        return this.instanceIdentity;
    }

    /**
     * get service's interface name.
     *
     * @return service's interface name.
     */
    public String getServiceInterfaceName() {
        return this.serviceInterface;
    }

    /**
     * get service's class name.
     *
     * @return service's class name.
     */
    public String getServiceImplClassName() {
        return this.serviceImplClass;
    }
}