package com.alibaba.confidentialcomputing.host;

/**
 * An enumeration of enclave type.
 * JavaEnclave supports three kinds of enclave, they are mock_jvm、mock_svm and tee_sdk.
 */
public enum EnclaveType {
    NONE,
    /**
     * A mock enclave environment, both host and enclave application run in the same
     * jvm environment, enclave services were discovered and loaded by SPI in host.
     */
    MOCK_IN_JVM,
    /**
     * A mock enclave environment, enclave application was compiled to machine code
     * by graalvm svm compiler, host application runs in jvm environment, and enclave
     * package was loaded by host.
     */
    MOCK_IN_SVM,
    /**
     * An enclave based on Intel's SGX2, with Alibaba Cloud's TEESdk. Enclave application
     * was compiled to machine code and lint together with TEESdk's underlying libs,
     * host application runs in jvm environment, and enclave package were loaded by host.
     */
    TEE_SDK,
    /**
     * An enclave based on Intel's SGX2, with OCCLUM Libos. Enclave application
     * was compiled to .class files and packaged as a jar file, there is a jvm runs based
     * on enclave's occlum libos. host application runs in jvm environment, and enclave
     * package were loaded by host.
     */
    EMBEDDED_LIB_OS,
}