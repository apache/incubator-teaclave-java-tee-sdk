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

package org.apache.teaclave.javasdk.tool.optimize;

import java.util.ServiceLoader;

public class LoopCase {
    public void foo() {
        ServiceLoader<TestServices.EnclaveServiceExample> exampleLoader = ServiceLoader.load(TestServices.EnclaveServiceExample.class);
        TestServices.EnclaveServiceExample example = exampleLoader.findFirst().get();
        int max = 100;
        int i = 0;
        while (i < max) {
            example.bar();
            i++;
        }
    }

    public void doSomething(){
        ServiceLoader<TestServices.NormalServiceExample> exampleLoader = ServiceLoader.load(TestServices.NormalServiceExample.class);
        TestServices.NormalServiceExample example = exampleLoader.findFirst().get();
        int max = 100;
        int i = 0;
        while (i < max) {
            example.bar();
            i++;
        }
    }
}
