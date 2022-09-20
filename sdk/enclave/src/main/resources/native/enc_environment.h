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

typedef struct enc_data_struct{
    //char array is used as byte array to store serialized data
    char* data;
    int data_len;
} enc_data_t;

typedef struct callback_functions_struct{
    /*
     * This method is invoked inside java_enclave_invoke method's exception catch
     * section, when the execution is aborted by exceptions. The caller side can
     * decide what to do with the exception.
     * Exception details are passed back with parameters.
     */
    void (*exception_handler)(char* err_msg, char* stack_trace, char* exception_name);

    char* (*memcpy_char_pointer)(char* src, int len);

    /*
     * Points to an available pseudorandom number generating function.
     */
    int (*get_random_number)(void* data, long size);
} callbacks_t;

long physical_page_size();
long physical_page_number();
long virtual_page_size();
