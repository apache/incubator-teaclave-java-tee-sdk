// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.teaclave.javasdk.enclave.framework;

import org.apache.teaclave.javasdk.common.ServiceHandler;
import org.apache.teaclave.javasdk.common.exception.ConfidentialComputingException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class maintains the enclave context, i.e. the cached service instances.
 */
final class EnclaveContext {

    private static final EnclaveContext instance = new EnclaveContext();

    public static EnclaveContext getInstance() {
        return instance;
    }

    private final Map<String, Object> cachedServiceInstances;

    private final AtomicLong serviceCounter;

    private EnclaveContext() {
        cachedServiceInstances = new ConcurrentHashMap<>();
        serviceCounter = new AtomicLong(0);
    }

    public Object removeCache(String key) {
        return cachedServiceInstances.remove(key);
    }

    public void clearCache() {
        cachedServiceInstances.clear();
    }

    public int servicesSize() {
        return cachedServiceInstances.size();
    }

    /**
     * Lookup the service instance with the given identity checksum, service name and implementation class name from
     * cached map.
     *
     * @param instanceIdentity        service instance identity checksum
     * @param serviceName             the name of the service
     * @param implementationClassName the implementation class name
     * @return cached service instance
     */
    public Object lookupServiceInstance(String instanceIdentity, String serviceName, String implementationClassName) throws ConfidentialComputingException {
        if (!cachedServiceInstances.containsKey(instanceIdentity)) {
            throw new ConfidentialComputingException(String.format("No stored service %s with identity %s", serviceName, instanceIdentity));
        }
        Object serviceInstance = cachedServiceInstances.get(instanceIdentity);
        if (serviceInstance != null) {
            Class<?> serviceInstanceClass = serviceInstance.getClass();
            try {
                Class<?> interfaceClass = Class.forName(serviceName);
                if (!interfaceClass.isAssignableFrom(serviceInstanceClass)) {
                    throw new ConfidentialComputingException(String.format("Cached service instance with identity %s doesn't implement the interface %s.",
                            instanceIdentity, serviceName));
                }
            } catch (ClassNotFoundException e) {
                throw new ConfidentialComputingException(String.format("Can't find the interface class %s.",
                        serviceName), e);
            }

            String cachedImplementationClassName = serviceInstanceClass.getName();
            if (!cachedImplementationClassName.equals(implementationClassName)) {
                throw new ConfidentialComputingException(String.format("Implementation class does not match, expected is %s, but found is %s.", implementationClassName, cachedImplementationClassName));
            }
        }
        return serviceInstance;
    }

    public ServiceHandler[] loadService(Class<?> service) {
        List<ServiceHandler> serviceHandlerList = new ArrayList<>();
        for (Object currentServiceInstance : ServiceLoader.load(service)) {
            String identity = String.valueOf(serviceCounter.addAndGet(1));
            cachedServiceInstances.put(identity, currentServiceInstance);
            serviceHandlerList.add(new ServiceHandler(service.getName(), currentServiceInstance.getClass().getName(), identity));
        }
        return serviceHandlerList.toArray(new ServiceHandler[0]);
    }
}
