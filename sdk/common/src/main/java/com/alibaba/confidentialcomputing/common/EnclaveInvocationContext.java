package com.alibaba.confidentialcomputing.common;

import java.io.Serializable;

/**
 * This class stores a method's necessary information for reflection
 * call, including the service instance's unique instanceIdentity, interface name, class name,
 * method name and its parameters.
 */
public class EnclaveInvocationContext implements Serializable {
    private static final long serialVersionUID = 6878585714134748604L;

    private final ServiceHandler serviceHandler;
    private final String methodName;
    private final String[] parameterTypes;
    private final Object[] arguments;

    public EnclaveInvocationContext() {
        this.serviceHandler = null;
        this.methodName = null;
        this.parameterTypes = null;
        this.arguments = null;
    }

    public EnclaveInvocationContext(ServiceHandler serviceHandler,
                                    String methodName,
                                    String[] parameterTypes,
                                    Object[] arguments) {
        this.serviceHandler = serviceHandler;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.arguments = arguments;
    }

    public EnclaveInvocationContext(ServiceHandler serviceHandler) {
        this.methodName = null;
        this.parameterTypes = null;
        this.arguments = null;
        this.serviceHandler = serviceHandler;
    }

    /**
     * get service handler.
     *
     * @return service handler.
     */
    public ServiceHandler getServiceHandler() {
        return serviceHandler;
    }

    /**
     * get the method's name.
     *
     * @return method's name.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * get all parameters' type.
     *
     * @return parameters' type information.
     */
    public String[] getParameterTypes() {
        return parameterTypes;
    }

    /**
     * get all arguments' value.
     *
     * @return arguments' value information.
     */
    public Object[] getArguments() {
        return arguments;
    }
}
