package com.alibaba.confidentialcomputing.enclave;

import com.alibaba.confidentialcomputing.enclave.testservice.EnclaveMem;
import com.alibaba.confidentialcomputing.enclave.testservice.MemService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigMemTest {

    @TestTarget(ConfigMemTest.class)
    static class MemConfigPreparation extends NativeImageTest {

        @Override
        public SVMCompileElements specifyTestClasses() {
            SVMCompileElements ret = new SVMCompileElements();
            // Specify the service file
            ret.addServices("META-INF/services/com.alibaba.confidentialcomputing.enclave.testservice.MemService");

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
    }

    private static final String MEM_SERVICE = "com.alibaba.confidentialcomputing.enclave.testservice.MemService";
    private static final String ENC_MEM = "com.alibaba.confidentialcomputing.enclave.testservice.EnclaveMem";

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
