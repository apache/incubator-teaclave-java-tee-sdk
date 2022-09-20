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

package org.apache.teaclave.javasdk.enclave;

import org.apache.teaclave.javasdk.enclave.testservice.EnclaveMem;
import org.apache.teaclave.javasdk.enclave.testservice.MemService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigMemTest {

    @TestTarget(ConfigMemTest.class)
    static class MemConfigPreparation extends NativeImageTest {

        @Override
        public SVMCompileElements specifyTestClasses() {
            SVMCompileElements ret = new SVMCompileElements();
            // Specify the service file
            ret.addServices("META-INF/services/org.apache.teaclave.javasdk.enclave.testservice.MemService");

            // Specify the classes need to be statically compiled into native image for this test
            ret.addClasses(
                    MemService.class, EnclaveMem.class
            );

            return ret;
        }

        @Override
        protected Collection<String> addMacros(){
            return List.of("-DPAGE_SIZE=2048",
                    "-DHEAP_PAGES=24000");
        }

       /* @Override
        public List<String> extraSVMOptions() {
            return List.of(*//*"--debug-attach:7788",
                    "-H:Dump=:3",
                    "-H:MethodFilter=com.oracle.svm.core.posix.PosixVirtualMemoryProvider.getPageSize",
                    "-H:MethodFilter=org.apache.teaclave.javasdk.enclave.system.EnclaveVirtualMemoryProvider.getVPageSize"*//*
                    );
        }*/
    }

    private static final String MEM_SERVICE = "org.apache.teaclave.javasdk.enclave.testservice.MemService";
    private static final String ENC_MEM = "org.apache.teaclave.javasdk.enclave.testservice.EnclaveMem";

    @BeforeAll
    public static void prepareLibraries() {
        new MemConfigPreparation().prepareNativeLibraries();
    }

    @BeforeEach
    public void setup() {
        EnclaveTestHelper.createIsolate();
    }

    @AfterEach
    public void teardown() {
        EnclaveTestHelper.destroyIsolate();
    }

    @Test
    public void test() {
        String id = EnclaveTestHelper.loadAndGetService(MEM_SERVICE, ENC_MEM, 1);
        long ret = (Long)EnclaveTestHelper.call(id, MEM_SERVICE, ENC_MEM, "getSize", EnclaveTestHelper.EMPTY_STRING_ARRAY, EnclaveTestHelper.EMPTY_OBJECT_ARRAY);
        assertEquals(49152000, ret);
    }
}
