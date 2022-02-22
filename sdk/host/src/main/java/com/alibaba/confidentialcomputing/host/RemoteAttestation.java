package com.alibaba.confidentialcomputing.host;

import com.alibaba.confidentialcomputing.host.exception.RemoteAttestationException;

import java.util.Random;

/**
 * RemoteAttestation mainly provides enclave's remote attestation generation and verification.
 */
public final class RemoteAttestation {
    // normalizeUserData format userData to make sure it's a buffer with 64 bytes.
    private static byte[] normalizeUserData(byte[] userData) throws RemoteAttestationException {
        byte[] result = null;
        if (userData == null) {
            result = new byte[64];
            new Random().nextBytes(result);
        } else if (userData.length < 64) {
            result = new byte[64];
            System.arraycopy(userData, 0, result, 0, userData.length);
        } else if (userData.length > 64) {
            throw new RemoteAttestationException("enclave remote attestation user data length exceeds 64 bytes.");
        }
        return result;
    }

    /**
     * Generate enclave's remote attestation report, the report is signed by the enclave platform
     * in TEE, it's used to verify enclave's validation.
     * <p>
     *
     * @param enclave  an enclave instance.
     * @param userData provided as user identification, its length must be 64 bytes.
     *                 If userData is null, JavaEnclave will generate a random buffer
     *                 with 64 length bytes for it.
     *                 If userData's length exceeds 64 bytes, RemoteAttestationException
     *                 will be thrown.
     *                 If userData's length is less than 64 bytes, padding was filled in the tail.
     * @return Remote attestation report data.
     * @throws RemoteAttestationException {@link RemoteAttestationException} If enclave remote
     *                                    attestation generated failed.
     */
    public static AttestationReport generateAttestationReport(Enclave enclave, byte[] userData) throws RemoteAttestationException {
        if (!(enclave instanceof AbstractEnclave)) {
            throw new RemoteAttestationException("enclave instance class type is not AbstractEnclave.");
        }
        return ((AbstractEnclave) enclave).generateAttestationReport(normalizeUserData(userData));
    }

    /**
     * Verify an enclave's validation according to giving report data signed by one enclave.
     * <p>
     *
     * @param report signed data in an enclave and its tee type info.
     * @return Zero means enclave is valid, Other value means enclave is invalid.
     */
    public static int verifyAttestationReport(AttestationReport report) throws RemoteAttestationException {
        if (report.getEnclaveType() != EnclaveType.TEE_SDK) {
            throw new RemoteAttestationException("enclaveType must be TEE_SDK.");
        }
        return TeeSdkEnclave.verifyAttestationReport(report.getReport());
    }
}
