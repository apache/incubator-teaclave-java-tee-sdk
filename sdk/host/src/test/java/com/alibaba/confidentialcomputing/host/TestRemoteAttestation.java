package com.alibaba.confidentialcomputing.host;

import com.alibaba.confidentialcomputing.host.exception.EnclaveCreatingException;
import com.alibaba.confidentialcomputing.host.exception.RemoteAttestationException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class TestRemoteAttestation {
    @Test
    void testRemoteAttestation() throws EnclaveCreatingException {
        Enclave mockInJvmEnclave = new MockInJvmEnclave();
        assertThrows(RemoteAttestationException.class, () -> RemoteAttestation.generateAttestationReport(mockInJvmEnclave, null));
        assertThrows(RemoteAttestationException.class, () -> RemoteAttestation.verifyAttestationReport(new AttestationReport(EnclaveType.MOCK_IN_JVM, null)));
        assertThrows(RemoteAttestationException.class, () -> RemoteAttestation.verifyAttestationReport(new AttestationReport(EnclaveType.MOCK_IN_SVM, null)));
    }

    @Test
    void testNormalizeUserData() throws Exception {
        Class<RemoteAttestation> clazz = RemoteAttestation.class;
        Method method = clazz.getDeclaredMethod("normalizeUserData", byte[].class);
        method.setAccessible(true);

        byte[] parameter = null;
        Object result = method.invoke(null, (Object) parameter);
        assertEquals(((byte[]) result).length, 64);

        parameter = new byte[32];
        result = method.invoke(null, parameter);
        assertEquals(((byte[]) result).length, 64);

        byte[] finalParameter = new byte[65];
        assertThrows(InvocationTargetException.class, () -> method.invoke(null, finalParameter));
    }

    @Test
    void testAttestationReport() throws Exception {
        byte[] quote = new byte[4];
        for (int index = 0; index < quote.length; index++) {
            quote[index] = (byte) 0x5f;
        }
        AttestationReport report = new AttestationReport(EnclaveType.TEE_SDK, quote);
        byte[] serializedReport = report.toByteArray();
        AttestationReport deserializedReport = AttestationReport.fromByteArray(serializedReport);
        assertEquals(EnclaveType.TEE_SDK, deserializedReport.getEnclaveType());
        for (int index = 0; index < quote.length; index++) {
            assertEquals(quote[index], (deserializedReport.getQuote())[index]);
        }
    }
}
