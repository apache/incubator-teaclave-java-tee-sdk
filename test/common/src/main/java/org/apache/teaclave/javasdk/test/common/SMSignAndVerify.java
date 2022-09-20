package org.apache.teaclave.javasdk.test.common;

import org.apache.teaclave.javasdk.common.annotations.EnclaveService;

@EnclaveService
public interface SMSignAndVerify {
    Boolean smSignAndVerify(String plaintext) throws Exception;
}
