#!/bin/bash

BUILD_IMAGE=javaenclave_build
BUILD_TAG=v0.1.2

SHELL_FOLDER=$(cd "$(dirname "$0")";pwd)

cd "${SHELL_FOLDER}"

WORKDIR=$(dirname $(dirname "$PWD"))

# check target images exist or not, build it if not.
if [[ "$(docker images -q ${BUILD_IMAGE}:${BUILD_TAG} 2> /dev/null)" == "" ]]; then
  # Get the customized Graal VM from git@gitlab.alibaba-inc.com:graal/SGXGraalVM.git
  # This should be replaced to the offical version when all patches are accepted by the Graal community
  wget https://graal.oss-cn-beijing.aliyuncs.com/graal-enclave/JDK11-22.0.0/graalvm-enclave-22.0.0.tar
  wget http://graal.oss-cn-beijing.aliyuncs.com/graal-enclave/x86_64-linux-musl-native.tgz
  wget http://graal.oss-cn-beijing.aliyuncs.com/graal-enclave/zlib-1.2.12.tar.gz
  docker build -t ${BUILD_IMAGE}:${BUILD_TAG} .
  rm -f graalvm-enclave-22.0.0.tar
  rm -f x86_64-linux-musl-native.tgz
  rm -f zlib-1.2.12.tar.gz
fi

# test JavaEnclave's unit test cases and samples
docker run -i --rm --privileged --network host                    \
-w "${WORKDIR}"                                                   \
-v "${HOME}"/.m2:/root/.m2 -v "${WORKDIR}":"${WORKDIR}"           \
${BUILD_IMAGE}:${BUILD_TAG} /bin/bash build.sh

