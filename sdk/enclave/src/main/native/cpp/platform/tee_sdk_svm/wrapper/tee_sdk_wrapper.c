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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <sgx_trts.h>

#include <graal_isolate.h>
#include <enc_environment.h>
#include <enc_exported_symbol.h>

#include "tee_sdk_symbol.h"
#include "tee_sdk_wrapper.h"

typedef int (*enclave_calling_stub)(uint64_t isolate, enc_data_t* input, enc_data_t* output, callbacks_t* callback);

char* alloc_memory_from_host(char* src, int len) {
    int flag = 0;
    char *ptr = 0;
    ocall_malloc(&flag, len, (void*)&ptr);
    // ocall malloc buffer failed.
    if (flag != 0x0) { return NULL; }
    memcpy(ptr, src, len);
    return (char*)ptr;
}

void tee_sdk_exception_callback(char* err_msg, char* stack_trace, char* exception_name) {
    printf("err_msg=%s\n", err_msg);
    printf("stack_trace=%s\n", stack_trace);
    printf("exception_name=%s\n", exception_name);
}

int tee_sdk_random(void* data, long size) {
    return (int)sgx_read_rand(data, (size_t)size);
}

int enclave_svm_isolate_create(void* isolate, void* isolateThread, int flag, char* args) {
    graal_isolate_t* isolate_t;
    graal_isolatethread_t* thread_t;

    // Implicitly set graal_create_isolate_params_t param as NULL.
    enable_trace_symbol_calling = flag;
    int argc = 2;
    char* parameters[2];
    parameters[0] = NULL;
    parameters[1] = args;
    int ret = create_isolate_with_params(argc, parameters, &isolate_t, &thread_t);
    *(uint64_t*)isolate = (uint64_t)isolate_t;
    *(uint64_t*)isolateThread = (uint64_t)thread_t;
    return ret;
}

int enclave_svm_isolate_destroy(uint64_t isolateThread) {
    return graal_detach_all_threads_and_tear_down_isolate((graal_isolatethread_t*)isolateThread);
}

int enclave_svm_calling_entry(uint64_t isolate, void* input, size_t input_length, void* output, size_t* output_length, enclave_calling_stub stub) {
    enc_data_t request;
    enc_data_t response;

    request.data = (char*) input;
    request.data_len = input_length;
    response.data = NULL;
    response.data_len = 0x0;

    callbacks_t callback_methods;
    callback_methods.memcpy_char_pointer = &alloc_memory_from_host;
    callback_methods.exception_handler = &tee_sdk_exception_callback;
    callback_methods.get_random_number = &tee_sdk_random;

    int ret = stub(isolate, &request, &response, &callback_methods);
    if(ret != 0) { return ret; }

    *(int64_t*)output = (int64_t)response.data;
    *output_length = response.data_len;

    return 0x0;
}

int load_enclave_svm_services(uint64_t isolate, void* input, size_t input_length, void* output, size_t* output_length) {
    return enclave_svm_calling_entry(isolate, input, input_length, output, output_length, (enclave_calling_stub)java_loadservice_invoke);
}

int invoke_enclave_svm_service(uint64_t isolate, void* input, size_t input_length, void* output, size_t* output_length) {
    return enclave_svm_calling_entry(isolate, input, input_length, output, output_length, (enclave_calling_stub)java_enclave_invoke);
}

int unload_enclave_svm_service(uint64_t isolate, void* input, size_t input_length, void* output, size_t* output_length) {
    return enclave_svm_calling_entry(isolate, input, input_length, output, output_length, (enclave_calling_stub)java_unloadservice_invoke);
}