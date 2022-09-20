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

enclave_target_path=${ENCLAVE_BASE_DIR}/target

user_occlum_enclave_config_file=/opt/javaenclave/config/template/java_enclave_configure.json
if [ -f "${ENCLAVE_BASE_DIR}/src/main/resources/java_enclave_configure.json" ]; then
    user_occlum_enclave_config_file=${ENCLAVE_BASE_DIR}/src/main/resources/java_enclave_configure.json
fi

# parse enclave with dependencies jar file name.
# shellcheck disable=SC2061
# shellcheck disable=SC2185
# shellcheck disable=SC2035
pushd "${enclave_target_path}"
enclave_jar_name=$(find -name *-jar-with-dependencies.jar)
if [[ -z $enclave_jar_name ]]; then
    echo "enclave with dependencies jar file is empty."
    exit 1
fi
popd

# create lib os enclave workspace.
mkdir -p "${ENCLAVE_BASE_DIR}"/target/enclave_workspace/occlum_instance
rm -rf "${ENCLAVE_BASE_DIR}"/target/enclave_workspace/occlum_instance/*

pushd "${enclave_target_path}"/enclave_workspace/occlum_instance
# create occlum instance and build occlum image.
occlum init

# update Occlum.json according to user's config file embedded_libos_enclave.json
debuggable=$(< "${user_occlum_enclave_config_file}" jq -r '.debuggable')
occlum_max_thread_num=$(< "${user_occlum_enclave_config_file}" jq -r '.enclave_max_thread')
user_space_size=$(< "${user_occlum_enclave_config_file}" jq -r '.enclave_max_epc_memory_size_MB')
user_space_size=${user_space_size}MB

new_json="$(jq --arg user_space_size "$user_space_size"     \
               --argjson occlum_max_thread_num "$occlum_max_thread_num" \
               --argjson debuggable "$debuggable" \
              '.resource_limits.user_space_size = $user_space_size |
               .resource_limits.max_num_of_threads = $occlum_max_thread_num |
               .metadata.debuggable = $debuggable |
               .entry_points = [ "/usr/lib/dragonwell11/jre/bin" ] |
               .env.default = [ "LD_LIBRARY_PATH=/usr/lib/dragonwell11/jre/lib/server:/usr/lib/dragonwell11/jre/lib:/usr/lib/dragonwell11/jre/../lib" ]' Occlum.json)"

echo "${new_json}" > Occlum.json

# prepare zlib for jvm in libos occlum enclave.
cp /opt/occlum/toolchains/gcc/x86_64-linux-musl/lib/libz.so.1.2.11 ./image/lib/libz.so.1

# prepare occlum_dcap for occlum remote attestation.
cp /opt/occlum/toolchains/dcap_lib/musl/libocclum_dcap.so.0.1.0 ./image/lib/libocclum_dcap.so.0.1.0

# prepare occlum remote attestation jni.so
mkdir -p ./image/usr/lib
cp -r /opt/javaenclave/bin/platform/libos_occlum_enclave/libos_occlum_enclave_attestation ./image/usr/lib

# prepate musl-based jvm in libos occlum enclave.
mkdir -p ./image/usr/lib/dragonwell11/jre
cp -r /root/tools/dragonwell-11.0.16.12+8-GA/. ./image/usr/lib/dragonwell11/jre

# prepare app jar with dependencies in libos occlum enclave.
mkdir -p ./image/usr/app
cp -r "${enclave_target_path}"/"${enclave_jar_name}" ./image/usr/app

# prepare private.pem for image signing.
if [[ -z ${ENCLAVE_PRIVATE_PEM_PATH} ]]; then
    openssl genrsa -out private.pem -3 3072
    occlum build --sign-key private.pem
else
    occlum build --sign-key "${ENCLAVE_PRIVATE_PEM_PATH}"
fi

if [ $debuggable ]; then
    # occlum package --debug > /dev/null
    occlum package --debug > /dev/null 2>&1
else
    occlum package
fi

mv ./occlum_instance.tar.gz "${ENCLAVE_BASE_DIR}"/target/svm-output/lib_embedded_lib_os_enclave_load.tgz

popd