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

STAGE=$1

BUILD_IMAGE=java_enclave_build_ubuntu18.04
BUILD_TAG=v0.1.0

RELEASE_IMAGE=java_enclave_release_ubuntu18.04
RELEASE_TAG=v0.1.0

SHELL_FOLDER=$(cd "$(dirname "$0")";pwd)
cd "${SHELL_FOLDER}"

WORKDIR=$(dirname $(dirname $(dirname "$PWD")))
# Set PCCS for DCAP Remote Attestation.
PCCS_URL='https://sgx-dcap-server.cn-beijing.aliyuncs.com/sgx/certification/v3/'

function build_base_image() {
  # check base image exist or not, build it if not.
  if [[ "$(docker images -q ${BUILD_IMAGE}:${BUILD_TAG} 2> /dev/null)" == "" ]]; then
    echo "prepare for dependency"
    rm -rf tmpDownloadDir && mkdir -p tmpDownloadDir
    # download intel-sgx(branch: stdc_ex) and build it for tee sdk.
    ./teesdk/make.sh && cp -r ./teesdk/sgx_linux_x64_sdk_*.bin ./tmpDownloadDir
    # cp -r ./teesdk/sgx_linux_x64_sdk_*.bin ./tmpDownloadDir
    # download graalvm_22.2.0
    ./graalvm/make.sh && cp -r ./graalvm/graalvm-ce-java11-22.2.0.tar.gz ./tmpDownloadDir
    # cp -r ./graalvm/graalvm-ce-java11-22.2.0.tar.gz ./tmpDownloadDir
    # download zlib-1.2.11.tar.gz
    wget -P tmpDownloadDir https://zlib.net/fossils/zlib-1.2.11.tar.gz
    # download Alibaba Dragonwell_11_alpine
    wget -P tmpDownloadDir https://github.com/alibaba/dragonwell11/releases/download/dragonwell-standard-11.0.16.12_jdk-11.0.16-ga/Alibaba_Dragonwell_Standard_11.0.16.12.8_x64_alpine-linux.tar.gz

    echo "build base image"
    # Build JavaEnclave Base Image.
    docker build -t ${BUILD_IMAGE}:${BUILD_TAG} -f dockerfile_build .
    rm -rf tmpDownloadDir
  fi
}

function build_java_enclave() {
  echo "build java enclave in ubuntu18.04 container"
  build_base_image
  mkdir -p "${HOME}"/.m2
  docker run -i --rm --privileged --network host                    \
  -w "${WORKDIR}"                                                   \
  -v "${HOME}"/.m2:/root/.m2 -v "${WORKDIR}":"${WORKDIR}"           \
  -v /dev/sgx_enclave:/dev/sgx_enclave             \
  -v /dev/sgx_provision:/dev/sgx_provision         \
  ${BUILD_IMAGE}:${BUILD_TAG} /bin/bash build.sh $1
}

function build_release_image() {
  # check release image exist or not, build it if not.
  if [[ "$(docker images -q ${RELEASE_IMAGE}:${RELEASE_TAG} 2> /dev/null)" == "" ]]; then
    echo "build release image"
    tar zcvf javaenclave.tar.gz -C "${WORKDIR}"/release/opt javaenclave
    docker build -t ${RELEASE_IMAGE}:${RELEASE_TAG} -f dockerfile_release .
    rm -rf javaenclave.tar.gz
  fi
}

function test_java_enclave() {
  echo "test java enclave"
  build_release_image
  mkdir -p "${HOME}"/.m2
  # test JavaEnclave's unit test cases
  docker run -i --rm --privileged --network host                    \
  -w "${WORKDIR}"                                                   \
  -v "${HOME}"/.m2:/root/.m2 -v "${WORKDIR}":"${WORKDIR}"           \
  -e PCCS_URL=${PCCS_URL}                                           \
  -v /dev/sgx_enclave:/dev/sgx_enclave             \
  -v /dev/sgx_provision:/dev/sgx_provision         \
  ${RELEASE_IMAGE}:${RELEASE_TAG} /bin/bash build.sh $1
}

function collect_java_enclave_coverage() {
  echo "collect and analysis javaenclave's test coverage"
  build_release_image
  mkdir -p "${HOME}"/.m2
  # collect JavaEnclave's unit test code coverage
  docker run -i --rm --privileged --network host                    \
  -w "${WORKDIR}"                                                   \
  -v "${HOME}"/.m2:/root/.m2 -v "${WORKDIR}":"${WORKDIR}"           \
  -e PCCS_URL=${PCCS_URL}                                           \
  -v /dev/sgx_enclave:/dev/sgx_enclave             \
  -v /dev/sgx_provision:/dev/sgx_provision         \
  ${RELEASE_IMAGE}:${RELEASE_TAG} /bin/bash build.sh $1
}

function samples_java_enclave() {
  echo "samples java enclave"
  build_release_image
  mkdir -p "${HOME}"/.m2
  # samples JavaEnclave's samples
  docker run -i --rm --privileged --network host                    \
  -w "${WORKDIR}"                                                   \
  -v "${HOME}"/.m2:/root/.m2 -v "${WORKDIR}":"${WORKDIR}"           \
  -e PCCS_URL=${PCCS_URL}                                           \
  -v /dev/sgx_enclave:/dev/sgx_enclave             \
  -v /dev/sgx_provision:/dev/sgx_provision         \
  ${RELEASE_IMAGE}:${RELEASE_TAG} /bin/bash build.sh $1
}

function benchmark_java_enclave() {
  echo "benchmark java enclave"
  build_release_image
  mkdir -p "${HOME}"/.m2
  # benchmark JavaEnclave
  docker run -i --rm --privileged --network host                    \
  -w "${WORKDIR}"                                                   \
  -v "${HOME}"/.m2:/root/.m2 -v "${WORKDIR}":"${WORKDIR}"           \
  -e PCCS_URL=${PCCS_URL}                                           \
  -v /dev/sgx_enclave:/dev/sgx_enclave             \
  -v /dev/sgx_provision:/dev/sgx_provision         \
  ${RELEASE_IMAGE}:${RELEASE_TAG} /bin/bash build.sh $1
}

function collect_java_enclave_release() {
  echo "collect java enclave release"
  mkdir -p "${WORKDIR}"/release/opt/javaenclave
  cp -r "${WORKDIR}"/sdk/native/bin "${WORKDIR}"/release/opt/javaenclave
  cp -r "${WORKDIR}"/sdk/native/config "${WORKDIR}"/release/opt/javaenclave
  cp -r "${WORKDIR}"/sdk/native/script/build_app "${WORKDIR}"/release/opt/javaenclave
  mkdir -p "${WORKDIR}"/release/opt/javaenclave/jar/sdk
  mkdir -p "${WORKDIR}"/release/opt/javaenclave/jar/sdk/host
  mkdir -p "${WORKDIR}"/release/opt/javaenclave/jar/sdk/enclave
  mkdir -p "${WORKDIR}"/release/opt/javaenclave/jar/sdk/common
  cp -r "${WORKDIR}"/sdk/pom.xml "${WORKDIR}"/release/opt/javaenclave/jar/sdk
  cp -r "${WORKDIR}"/sdk/host/pom.xml "${WORKDIR}"/release/opt/javaenclave/jar/sdk/host
  cp -r "${WORKDIR}"/sdk/host/target/*.jar "${WORKDIR}"/release/opt/javaenclave/jar/sdk/host
  cp -r "${WORKDIR}"/sdk/enclave/pom.xml "${WORKDIR}"/release/opt/javaenclave/jar/sdk/enclave
  cp -r "${WORKDIR}"/sdk/enclave/target/*.jar "${WORKDIR}"/release/opt/javaenclave/jar/sdk/enclave
  cp -r "${WORKDIR}"/sdk/common/pom.xml "${WORKDIR}"/release/opt/javaenclave/jar/sdk/common
  cp -r "${WORKDIR}"/sdk/common/target/*.jar "${WORKDIR}"/release/opt/javaenclave/jar/sdk/common
  mkdir -p "${WORKDIR}"/release/opt/javaenclave/jar/archetype
  cp -r "${WORKDIR}"/archetype/pom.xml "${WORKDIR}"/release/opt/javaenclave/jar/archetype
  cp -r "${WORKDIR}"/archetype/target/*.jar "${WORKDIR}"/release/opt/javaenclave/jar/archetype
  mkdir -p "${WORKDIR}"/release/opt/javaenclave/jar/bouncycastle-native
  cp -r "${WORKDIR}"/third-party-libs/bouncycastle-native/pom.xml "${WORKDIR}"/release/opt/javaenclave/jar/bouncycastle-native
  cp -r "${WORKDIR}"/third-party-libs/bouncycastle-native/target/*.jar "${WORKDIR}"/release/opt/javaenclave/jar/bouncycastle-native
  cp -r "${WORKDIR}"/test "${WORKDIR}"/release/opt/javaenclave/
  cp -r "${WORKDIR}"/samples "${WORKDIR}"/release/opt/javaenclave/
  cp -r "${WORKDIR}"/coverage "${WORKDIR}"/release/opt/javaenclave/
  cp -r "${WORKDIR}"/benchmark "${WORKDIR}"/release/opt/javaenclave/
  build_release_image
}

function develop_java_enclave() {
  echo "develop java enclave"
  mkdir -p "${HOME}"/.m2
  build_base_image
  docker run -it --rm --privileged --network host                   \
  -w "${WORKDIR}"                                                   \
  -v "${HOME}"/.m2:/root/.m2 -v "${WORKDIR}":"${WORKDIR}"           \
  -e PCCS_URL=${PCCS_URL}                                           \
  -v /dev/sgx_enclave:/dev/sgx_enclave             \
  -v /dev/sgx_provision:/dev/sgx_provision         \
  ${BUILD_IMAGE}:${BUILD_TAG} /bin/bash
}

function develop_application() {
  echo "develop application based on JavaEnclave"
  build_release_image
  mkdir -p "${HOME}"/.m2
  docker run -it --rm --privileged --network host                   \
  -w "${WORKDIR}"                                                   \
  -v "${HOME}"/.m2:/root/.m2 -v "${WORKDIR}":"${WORKDIR}"           \
  -e PCCS_URL=${PCCS_URL}                                           \
  -v /dev/sgx_enclave:/dev/sgx_enclave             \
  -v /dev/sgx_provision:/dev/sgx_provision         \
  ${RELEASE_IMAGE}:${RELEASE_TAG} /bin/bash
}

function clean_java_enclave() {
  echo "clean java enclave"
  pushd "${WORKDIR}"
  # remove all files generated in building and developing.
  # remove all target dir.
  find -name target | xargs rm -rf
  # remove all .o and .so files
  find -name *.o | xargs rm -rf && find -name *.so | xargs rm -rf
  # remove release dir.
  rm -rf "${WORKDIR}"/release
  popd
}

if [ ! "$STAGE" ]; then
  # docker build java enclave base image.
  # build JavaEnclave in java enclave base image docker.
  # test JavaEnclave unit test case in java enclave release image docker.
  build_java_enclave build
  collect_java_enclave_release
  test_java_enclave test
  collect_java_enclave_coverage coverage
elif [ "build" = "$STAGE" ]; then
  # docker build java enclave base image.
  build_java_enclave build
elif [ "release" = "$STAGE" ]; then
  # docker build java enclave release image.
  collect_java_enclave_release
elif [ "test" = "$STAGE" ]; then
  # test JavaEnclave unit test case in java enclave release image docker.
  test_java_enclave test
elif [ "coverage" = "$STAGE" ]; then
  collect_java_enclave_coverage coverage
elif [ "samples" = "$STAGE" ]; then
  # run samples in java enclave release image docker.
  samples_java_enclave samples
elif [ "benchmark" = "$STAGE" ]; then
  # run benchmark in java enclave release image docker.
  benchmark_java_enclave benchmark
elif [ "develop" = "$STAGE" ]; then
  # enter java enclave base image docker and develop JavaEnclave.
  develop_java_enclave
elif [ "develop_app" = "$STAGE" ]; then
  # enter java enclave release image docker and develop application.
  develop_application
elif [ "clean" = "$STAGE" ]; then
  # remove all tmp files generated in build.
  clean_java_enclave
fi
