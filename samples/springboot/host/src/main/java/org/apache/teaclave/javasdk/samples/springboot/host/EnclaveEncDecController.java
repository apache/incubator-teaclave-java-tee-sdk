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

package org.apache.teaclave.javasdk.samples.springboot.host;

import org.apache.teaclave.javasdk.host.Enclave;
import org.apache.teaclave.javasdk.host.EnclaveFactory;
import org.apache.teaclave.javasdk.host.EnclaveType;
import org.apache.teaclave.javasdk.samples.springboot.common.SBEnclaveService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Iterator;

@RestController
public class EnclaveEncDecController {
    @RequestMapping("/enclaveEncDecService")
    public String enclaveEncDecService(String data) {
        try {
            Enclave enclave = EnclaveFactory.create(EnclaveType.TEE_SDK);
            Iterator<SBEnclaveService> services = enclave.load(SBEnclaveService.class);
            String result = services.next().encryptAndDecryptData(data);
            enclave.destroy();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
