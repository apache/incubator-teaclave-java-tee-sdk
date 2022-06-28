#!/bin/bash

enclave_target_path=${ENCLAVE_BASE_DIR}/target
user_occlum_enclave_config_file=${ENCLAVE_BASE_DIR}/src/main/resources/embedded_libos_enclave.json

# parse enclave with dependencies jar file name.
# shellcheck disable=SC2061
# shellcheck disable=SC2185
# shellcheck disable=SC2035
pushd "${enclave_target_path}"
enclave_jar_name=$(find -name *-jar-with-dependencies.jar)
if [[ -z $enclave_jar_name ]];
then
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
default_mmap_size=$(< "${user_occlum_enclave_config_file}" jq -r '.default_mmap_size')
occlum_kernel_heap_size=$(< "${user_occlum_enclave_config_file}" jq -r '.kernel_space_heap_size')
occlum_max_thread_num=$(< "${user_occlum_enclave_config_file}" jq -r '.max_num_of_threads')
user_space_size=$(< "${user_occlum_enclave_config_file}" jq -r '.user_space_size')

new_json="$(jq --arg default_mmap_size "$default_mmap_size" \
               --arg user_space_size "$user_space_size"     \
               --arg occlum_kernel_heap_size "$occlum_kernel_heap_size" \
               --argjson occlum_max_thread_num "$occlum_max_thread_num" \
               --argjson debuggable "$debuggable" \
              '.resource_limits.user_space_size = $user_space_size |
               .resource_limits.kernel_space_heap_size = $occlum_kernel_heap_size |
               .resource_limits.max_num_of_threads = $occlum_max_thread_num |
               .process.default_heap_size = "150MB" |
               .process.default_mmap_size = $default_mmap_size |
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
cp -r /root/tools/dragonwell-11.0.15.11+9-GA/. ./image/usr/lib/dragonwell11/jre

# prepare app jar with dependencies in libos occlum enclave.
mkdir -p ./image/usr/app
cp -r "${enclave_target_path}"/"${enclave_jar_name}" ./image/usr/app

# prepare private.pem for image signing.
if [[ -z ${ENCLAVE_PRIVATE_PEM_PATH} ]];
then
    openssl genrsa -out private.pem -3 3072
    occlum build --sign-key private.pem
else
    occlum build --sign-key "${ENCLAVE_PRIVATE_PEM_PATH}"
fi

if [ $debuggable ]
then
    # occlum package --debug > /dev/null
    occlum package --debug > /dev/null 2>&1
else
    occlum package
fi

mv ./occlum_instance.tar.gz "${ENCLAVE_BASE_DIR}"/target/svm-output/lib_embedded_lib_os_enclave_load.tgz

popd