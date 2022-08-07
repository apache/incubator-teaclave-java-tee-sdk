#!/bin/bash

MODE=$1

BUILD_IMAGE=javaenclave_build
BUILD_TAG=v0.1.12

SHELL_FOLDER=$(cd "$(dirname "$0")";pwd)

cd "${SHELL_FOLDER}"

WORKDIR=$(dirname $(dirname "$PWD"))

# check target images exist or not, build it if not.
if [[ "$(docker images -q ${BUILD_IMAGE}:${BUILD_TAG} 2> /dev/null)" == "" ]]; then
  # We have built and packaged GraalVM 22.2.0 from source code and then uploaded to OSS, the official release of GraalVM CE required to manually install native-image component.
  wget http://graal.oss-cn-beijing.aliyuncs.com/graal-enclave/JDK11-22.2.0/graalvm-ce-java11-22.2.0.tar
  wget http://graal.oss-cn-beijing.aliyuncs.com/graal-enclave/zlib-1.2.11.tar.gz
  wget http://graal.oss-cn-beijing.aliyuncs.com/graal-enclave/settings_taobao.xml -O settings.xml
  wget https://dragonwell.oss-cn-shanghai.aliyuncs.com/11/tee_java/dependency/sgx_linux_x64_sdk_2.17.100.1.bin
  wget https://dragonwell.oss-cn-shanghai.aliyuncs.com/11.0.15.11.9/Alibaba_Dragonwell_11.0.15.11.9_x64_alpine-linux.tar.gz
  docker build -t ${BUILD_IMAGE}:${BUILD_TAG} .
  rm -f graalvm-ce-java11-22.2.0.tar
  rm -f settings.xml
  rm -f zlib-1.2.11.tar.gz
  rm -f sgx_linux_x64_sdk_2.17.100.1.bin
  rm -f Alibaba_Dragonwell_11.0.15.11.9_x64_alpine-linux.tar.gz
fi

# Set PCCS for DCAP Remote Attestation.
PCCS_URL='https://sgx-dcap-server.cn-beijing.aliyuncs.com/sgx/certification/v3/'

if [ ! "$MODE" -o "build" = "$MODE" ]; then
	echo "enter build mode"
  # test JavaEnclave's unit test cases and samples
  docker run -i --rm --privileged --network host                    \
  -w "${WORKDIR}"                                                   \
  -v "${HOME}"/.m2:/root/.m2 -v "${WORKDIR}":"${WORKDIR}"           \
  -e PCCS_URL=${PCCS_URL}                                           \
  -v /dev/sgx_enclave:/dev/sgx/enclave             \
  -v /dev/sgx_provision:/dev/sgx/provision         \
  ${BUILD_IMAGE}:${BUILD_TAG} /bin/bash build.sh
elif [ "develop" = "$MODE" ]; then
	echo "enter develop mode"
  # /bin/bash build.sh and then develop your project.
  docker run -it --rm --privileged --network host                   \
  -w "${WORKDIR}"                                                   \
  -v "${HOME}"/.m2:/root/.m2 -v "${WORKDIR}":"${WORKDIR}"           \
  -e PCCS_URL=${PCCS_URL}                                           \
  -v /dev/sgx_enclave:/dev/sgx/enclave             \
  -v /dev/sgx_provision:/dev/sgx/provision         \
  ${BUILD_IMAGE}:${BUILD_TAG} /bin/bash
fi
