package com.alibaba.samples.helloworld.host;

import com.alibaba.confidentialcomputing.host.Enclave;
import com.alibaba.confidentialcomputing.host.EnclaveFactory;
import com.alibaba.confidentialcomputing.host.EnclaveType;
import com.alibaba.confidentialcomputing.host.exception.EnclaveCreatingException;
import com.alibaba.confidentialcomputing.host.exception.EnclaveDestroyingException;
import com.alibaba.confidentialcomputing.host.exception.ServicesLoadingException;
import com.alibaba.samples.helloworld.common.Service;

import java.util.Iterator;

public class Main {
    public static void main(String[] args) throws EnclaveCreatingException, ServicesLoadingException, EnclaveDestroyingException {
        EnclaveType[] enclaveTypes = {
                EnclaveType.MOCK_IN_JVM,
                EnclaveType.MOCK_IN_SVM,
                EnclaveType.TEE_SDK,
                EnclaveType.EMBEDDED_LIB_OS};

        for (EnclaveType enclaveType : enclaveTypes) {
            Enclave enclave = EnclaveFactory.create(enclaveType);
            Iterator<Service> services = enclave.load(Service.class);
            System.out.println(services.next().sayHelloWorld());
            enclave.destroy();
        }
    }
}
