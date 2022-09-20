#!/bin/bash

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

TEE_SDK_CONFIG_FILE_PATH=$1
TEE_SDK_BUILD_WORKSPACE=$2

# prepare for TeeSDK.conf
cp /opt/javaenclave/config/template/TeeSDK.conf "${TEE_SDK_BUILD_WORKSPACE}"
user_tee_sdk_enclave_config_file=/opt/javaenclave/config/template/java_enclave_configure.json
if [[ -f "${TEE_SDK_CONFIG_FILE_PATH}/java_enclave_configure.json" ]]; then
    user_tee_sdk_enclave_config_file=${TEE_SDK_CONFIG_FILE_PATH}/java_enclave_configure.json
fi
max_thread_num=$(< "${user_tee_sdk_enclave_config_file}" jq -r '.enclave_max_thread')
user_space_size=$(< "${user_tee_sdk_enclave_config_file}" jq -r '.enclave_max_epc_memory_size_MB')
user_space_size=$((user_space_size*1024*1024))
user_space_size=$(printf "%x" $user_space_size)
sed -i "s/<TCSNum>[0-9]*<\/TCSNum>/<TCSNum>${max_thread_num}<\/TCSNum>/g" "${TEE_SDK_BUILD_WORKSPACE}"/TeeSDK.conf
sed -i "s/<HeapMaxSize>0x[0-9]*<\/HeapMaxSize>/<HeapMaxSize>0x${user_space_size}<\/HeapMaxSize>/g" "${TEE_SDK_BUILD_WORKSPACE}"/TeeSDK.conf