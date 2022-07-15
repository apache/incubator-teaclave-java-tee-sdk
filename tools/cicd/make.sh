#!/bin/bash

BUILD_IMAGE=javaenclave_build
BUILD_TAG=v0.1.8

SHELL_FOLDER=$(cd "$(dirname "$0")";pwd)

cd "${SHELL_FOLDER}"

WORKDIR=$(dirname $(dirname "$PWD"))

# check target images exist or not, build it if not.
if [[ "$(docker images -q ${BUILD_IMAGE}:${BUILD_TAG} 2> /dev/null)" == "" ]]; then
  # Get the customized Graal VM from git@gitlab.alibaba-inc.com:graal/SGXGraalVM.git
  # This should be replaced to the offical version when all patches are accepted by the Graal community
  wget https://graal.oss-cn-beijing.aliyuncs.com/graal-enclave/JDK11-22.1.0/graalvm-enclave-22.1.0.tar
  wget http://graal.oss-cn-beijing.aliyuncs.com/graal-enclave/x86_64-linux-musl-native.tgz
  wget http://graal.oss-cn-beijing.aliyuncs.com/graal-enclave/zlib-1.2.11.tar.gz
  wget http://graal.oss-cn-beijing.aliyuncs.com/graal-enclave/settings_taobao.xml -O settings.xml
  wget https://dragonwell.oss-cn-shanghai.aliyuncs.com/11/tee_java/dependency/sgx_linux_x64_sdk_2.17.100.0.bin
  docker build -t ${BUILD_IMAGE}:${BUILD_TAG} .
  rm -f graalvm-enclave-22.1.0.tar
  rm -f x86_64-linux-musl-native.tgz
  rm -f zlib-1.2.11.tar.gz
  rm -f sgx_linux_x64_sdk_2.17.100.0.bin
fi

# test JavaEnclave's unit test cases and samples
docker run -i --rm --privileged --network host                    \
-w "${WORKDIR}"                                                   \
-v "${HOME}"/.m2:/root/.m2 -v "${WORKDIR}":"${WORKDIR}"           \
-v /dev/sgx_enclave:/dev/sgx/enclave             \
-v /dev/sgx_provision:/dev/sgx/provision         \
${BUILD_IMAGE}:${BUILD_TAG} /bin/bash build.sh

