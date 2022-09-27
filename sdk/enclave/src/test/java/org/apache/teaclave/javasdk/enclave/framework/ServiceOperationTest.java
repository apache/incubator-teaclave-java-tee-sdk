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

import org.apache.teaclave.javasdk.common.EnclaveInvocationResult;
import org.apache.teaclave.javasdk.common.ServiceHandler;
import org.apache.teaclave.javasdk.enclave.testservice.NumericMath;
import org.apache.teaclave.javasdk.enclave.testservice.PointMath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.apache.teaclave.javasdk.enclave.EnclaveTestHelper.MATH_SERVICE;
import static org.apache.teaclave.javasdk.enclave.EnclaveTestHelper.isInNativeImage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * This class tests loading and unloading service.
 */
public class ServiceOperationTest {

    @BeforeAll
    public static void svmCheck(){
        assumeFalse(isInNativeImage());
    }

    @Test
    public void testLoadingAndUnloading() {
        LoadServiceInvoker loadServiceInvoker = new LoadServiceInvoker();
        // Call loadService twice, check the service identities are different.
        EnclaveInvocationResult ret1 = loadServiceInvoker.callMethod(MATH_SERVICE);
        EnclaveInvocationResult ret2 = loadServiceInvoker.callMethod(MATH_SERVICE);
        ServiceHandler[] serviceHandlers1 = (ServiceHandler[]) ret1.getResult();
        ServiceHandler[] serviceHandlers2 = (ServiceHandler[]) ret2.getResult();

        // There should be two service implementation instances
        assertEquals(3, serviceHandlers1.length);
        // They should have the same order as defined in the service configuration file
        assertTrue(serviceHandlers1[0].getServiceImplClassName().equals(NumericMath.class.getName()));
        assertTrue(serviceHandlers1[1].getServiceImplClassName().equals(PointMath.class.getName()));

        // The second group of service implementations should be the same
        assertEquals(3, serviceHandlers2.length);
        assertTrue(serviceHandlers2[0].getServiceImplClassName().equals(NumericMath.class.getName()));
        assertTrue(serviceHandlers2[1].getServiceImplClassName().equals(PointMath.class.getName()));

        // Compare the service instance identities, should be different
        assertNotEquals(serviceHandlers1[0].getInstanceIdentity(), serviceHandlers2[0].getInstanceIdentity());
        assertNotEquals(serviceHandlers1[1].getInstanceIdentity(), serviceHandlers2[1].getInstanceIdentity());

        // There are 4 services cached in EnclaveContext, and should be 3 left after unloading one.
        assertEquals(6, EnclaveContext.getInstance().servicesSize());
        UnloadServiceInvoker unloadServiceInvoker = new UnloadServiceInvoker();
        unloadServiceInvoker.callMethod(serviceHandlers1[0]);
        assertEquals(5, EnclaveContext.getInstance().servicesSize());
    }
}
