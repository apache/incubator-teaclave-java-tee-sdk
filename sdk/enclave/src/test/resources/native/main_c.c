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

#include <stdlib.h>
#include <string.h>
#include "enc_environment.h"
#ifdef MUSL
#include "libmusl_svmenclavesdk.h"
#else
#include "libsvm_enclave_sdk.h"
#endif

long physical_page_size(){
    return 4096;
}

long physical_page_number(){
    return 24576;
}

long virtual_page_size(){
    return 4096;
}

int main(int argc, char** argv){
    graal_isolatethread_t *thread = NULL;
    graal_isolate_t *isolate = NULL;
    int size = 2;
    char** parameters = (char **)malloc(size * sizeof(char*));
    parameters[0] = NULL;
    parameters[1] = "-Xmx100m";
    return create_isolate_with_params(size, parameters, &isolate, &thread);
}