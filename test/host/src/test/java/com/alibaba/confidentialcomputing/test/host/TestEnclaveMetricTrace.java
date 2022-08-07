package com.alibaba.confidentialcomputing.test.host;

import com.alibaba.confidentialcomputing.host.Enclave;
import com.alibaba.confidentialcomputing.host.EnclaveFactory;
import com.alibaba.confidentialcomputing.host.EnclaveType;
import com.alibaba.confidentialcomputing.host.MetricTrace;
import com.alibaba.confidentialcomputing.test.common.MetricTraceService;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class TestEnclaveMetricTrace {
    private String invertCharacter(String str) {
        byte[] content = new byte[str.length()];
        byte[] initial = str.getBytes();
        for (int i = 0x0; i < initial.length; i++) {
            content[i] = initial[initial.length - i - 1];
        }
        return new String(content);
    }

    @Test
    public void testEnclaveMetricTrace() throws Exception {
        MetricTrace.setEnclaveMetricTraceSwitch(true);
        String plaintext = "ABC_DEF_GHI_JKL_MNO_PQR_STU_VWX_YZ";
        EnclaveType[] types = new EnclaveType[] {
                EnclaveType.MOCK_IN_JVM,
                EnclaveType.MOCK_IN_SVM,
                EnclaveType.TEE_SDK,
                EnclaveType.EMBEDDED_LIB_OS};
        for (EnclaveType type : types) {
            Enclave enclave = EnclaveFactory.create(type);
            assertNotNull(enclave);
            Iterator<MetricTraceService> userServices = enclave.load(MetricTraceService.class);
            assertNotNull(userServices);
            assertTrue(userServices.hasNext());
            MetricTraceService service = userServices.next();
            String result = service.invertCharacter(plaintext);
            assertEquals(result, invertCharacter(plaintext));
            enclave.destroy();
        }
        MetricTrace.setEnclaveMetricTraceSwitch(false);

        Field flog = MetricTrace.class.getDeclaredField("logPath");
        flog.setAccessible(true);
        String logPath = (String) flog.get(null);
        assertNotNull(logPath);
        File file = new File(logPath);
        assertTrue(file.exists());
        InputStream in = new FileInputStream(logPath);
        byte[] buffer = new byte[in.available()];
        in.read(buffer);
        String str = new String(buffer);
        assertTrue(str.contains("enclave_creating_cost"));
        assertTrue(str.contains("enclave_destroying_cost"));
        assertTrue(str.contains("enclave_service_loading"));
        assertTrue(str.contains("TEE_SDK"));
        assertTrue(str.contains("EMBEDDED_LIB_OS"));
        assertTrue(file.delete());
    }
}
