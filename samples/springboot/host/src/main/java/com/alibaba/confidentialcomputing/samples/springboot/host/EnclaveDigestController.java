package com.alibaba.confidentialcomputing.samples.springboot.host;

import com.alibaba.confidentialcomputing.host.Enclave;
import com.alibaba.confidentialcomputing.host.EnclaveFactory;
import com.alibaba.confidentialcomputing.host.EnclaveType;
import com.alibaba.confidentialcomputing.samples.springboot.common.SBEnclaveService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Iterator;

@RestController
public class EnclaveDigestController {
    @RequestMapping("/enclaveDigestService")
    public String enclaveDigestService(String data) {
        try {
            Enclave enclave = EnclaveFactory.create(EnclaveType.TEE_SDK);
            Iterator<SBEnclaveService> services = enclave.load(SBEnclaveService.class);
            String result = services.next().digestData(data);
            enclave.destroy();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}