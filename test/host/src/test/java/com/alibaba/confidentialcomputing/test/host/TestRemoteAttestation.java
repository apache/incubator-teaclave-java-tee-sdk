package com.alibaba.confidentialcomputing.test.host;

import com.alibaba.confidentialcomputing.host.*;
import com.alibaba.confidentialcomputing.host.exception.EnclaveCreatingException;
import com.alibaba.confidentialcomputing.host.exception.EnclaveDestroyingException;
import com.alibaba.confidentialcomputing.host.exception.RemoteAttestationException;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class TestRemoteAttestation {

    private void remoteAttestation(EnclaveType type) throws EnclaveCreatingException, RemoteAttestationException, EnclaveDestroyingException {
        Enclave enclave = EnclaveFactory.create(type);
        assertNotNull(enclave);
        byte[] userData = new byte[64];
        new Random().nextBytes(userData);

        SGXAttestationReport report = (SGXAttestationReport) RemoteAttestation.generateAttestationReport(enclave, userData);
        assertEquals(report.getEnclaveType(), type);
        assertNotNull(report.getQuote());
        assertEquals(0, RemoteAttestation.verifyAttestationReport(report));
        assertNotNull(report.getMeasurementEnclave());
        assertNotNull(report.getMeasurementSigner());
        assertNotNull(report.getUserData());
        assertArrayEquals(userData, report.getUserData());
        enclave.destroy();
    }

    @Test
    public void testRemoteAttestation() throws Exception {
        remoteAttestation(EnclaveType.TEE_SDK);
        remoteAttestation(EnclaveType.EMBEDDED_LIB_OS);
    }
}
