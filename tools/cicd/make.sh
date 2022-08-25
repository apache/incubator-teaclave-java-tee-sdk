#!/bin/bash

STAGE=$1

BASE_IMAGE=javaenclave_base
BASE_TAG=v0.1.0

RELEASE_IMAGE=javaenclave_release
RELEASE_TAG=v0.1.0

SHELL_FOLDER=$(cd "$(dirname "$0")";pwd)
cd "${SHELL_FOLDER}"

WORKDIR=$(dirname $(dirname "$PWD"))
# Set PCCS for DCAP Remote Attestation.
PCCS_URL='https://sgx-dcap-server.cn-beijing.aliyuncs.com/sgx/certification/v3/'

function build_base_image() {
  # check base image exist or not, build it if not.
  if [[ "$(docker images -q ${BASE_IMAGE}:${BASE_TAG} 2> /dev/null)" == "" ]]; then
    echo "build base image"
    # We have built and packaged GraalVM 22.2.0 from source code and then uploaded to OSS, the official release of GraalVM CE required to manually install native-image component.
    wget -P tmpDownloadDir http://graal.oss-cn-beijing.aliyuncs.com/graal-enclave/JDK11-22.2.0/graalvm-ce-java11-22.2.0.tar
    wget -P tmpDownloadDir http://graal.oss-cn-beijing.aliyuncs.com/graal-enclave/zlib-1.2.11.tar.gz
    wget -P tmpDownloadDir https://dragonwell.oss-cn-shanghai.aliyuncs.com/11/tee_java/dependency/sgx_linux_x64_sdk_2.17.100.1.bin
    wget -P tmpDownloadDir https://dragonwell.oss-cn-shanghai.aliyuncs.com/11.0.15.11.9/Alibaba_Dragonwell_11.0.15.11.9_x64_alpine-linux.tar.gz
    wget http://graal.oss-cn-beijing.aliyuncs.com/graal-enclave/settings_taobao.xml -O tmpDownloadDir/settings.xml
    # Build JavaEnclave Base Image.
    docker build -t ${BASE_IMAGE}:${BASE_TAG} -f dockerfile_base .
    rm -rf tmpDownloadDir
  fi
}

function build_javaenclave() {
  echo "build javaenclave"
  build_base_image
  docker run -i --rm --privileged --network host                    \
  -w "${WORKDIR}"                                                   \
  -v "${HOME}"/.m2:/root/.m2 -v "${WORKDIR}":"${WORKDIR}"           \
  -v /dev/sgx_enclave:/dev/sgx/enclave             \
  -v /dev/sgx_provision:/dev/sgx/provision         \
  ${BASE_IMAGE}:${BASE_TAG} /bin/bash build.sh ${STAGE}
}

function build_release_image() {
  # check release image exist or not, build it if not.
  if [[ "$(docker images -q ${RELEASE_IMAGE}:${RELEASE_TAG} 2> /dev/null)" == "" ]]; then
    echo "build release image"
    build_javaenclave
    tar zcvf javaenclave.tar.gz -C ${WORKDIR}/release/opt javaenclave
    docker build -t ${RELEASE_IMAGE}:${RELEASE_TAG} -f dockerfile_release .
    rm -rf javaenclave.tar.gz
  fi
}

function test_javaenclave() {
  echo "test javaenclave"
  build_release_image
  # test JavaEnclave's unit test cases
  docker run -i --rm --privileged --network host                    \
  -w "${WORKDIR}"                                                   \
  -v "${HOME}"/.m2:/root/.m2 -v "${WORKDIR}":"${WORKDIR}"           \
  -e PCCS_URL=${PCCS_URL}                                           \
  -v /dev/sgx_enclave:/dev/sgx/enclave             \
  -v /dev/sgx_provision:/dev/sgx/provision         \
  ${RELEASE_IMAGE}:${RELEASE_TAG} /bin/bash build.sh ${STAGE}
}

function samples_javaenclave() {
  echo "samples javaenclave"
  build_release_image
  # samples JavaEnclave's samples
  docker run -i --rm --privileged --network host                    \
  -w "${WORKDIR}"                                                   \
  -v "${HOME}"/.m2:/root/.m2 -v "${WORKDIR}":"${WORKDIR}"           \
  -e PCCS_URL=${PCCS_URL}                                           \
  -v /dev/sgx_enclave:/dev/sgx/enclave             \
  -v /dev/sgx_provision:/dev/sgx/provision         \
  ${RELEASE_IMAGE}:${RELEASE_TAG} /bin/bash build.sh ${STAGE}
}

function benchmark_javaenclave() {
  echo "benchmark javaenclave"
  build_release_image
  # benchmark JavaEnclave
  docker run -i --rm --privileged --network host                    \
  -w "${WORKDIR}"                                                   \
  -v "${HOME}"/.m2:/root/.m2 -v "${WORKDIR}":"${WORKDIR}"           \
  -e PCCS_URL=${PCCS_URL}                                           \
  -v /dev/sgx_enclave:/dev/sgx/enclave             \
  -v /dev/sgx_provision:/dev/sgx/provision         \
  ${RELEASE_IMAGE}:${RELEASE_TAG} /bin/bash build.sh ${STAGE}
}

function collect_javaenclave_release() {
  echo "collect javaenclave release"
  mkdir -p ${WORKDIR}/release/opt/javaenclave
  cp -r ${WORKDIR}/sdk/native/bin ${WORKDIR}/release/opt/javaenclave
  cp -r ${WORKDIR}/sdk/native/config ${WORKDIR}/release/opt/javaenclave
  cp -r ${WORKDIR}/sdk/native/script/build_app ${WORKDIR}/release/opt/javaenclave
  mkdir -p ${WORKDIR}/release/opt/javaenclave/jar/sdk
  mkdir -p ${WORKDIR}/release/opt/javaenclave/jar/sdk/host
  mkdir -p ${WORKDIR}/release/opt/javaenclave/jar/sdk/enclave
  mkdir -p ${WORKDIR}/release/opt/javaenclave/jar/sdk/common
  cp -r ${WORKDIR}/sdk/pom.xml ${WORKDIR}/release/opt/javaenclave/jar/sdk
  cp -r ${WORKDIR}/sdk/host/pom.xml ${WORKDIR}/release/opt/javaenclave/jar/sdk/host
  cp -r ${WORKDIR}/sdk/host/target/*.jar ${WORKDIR}/release/opt/javaenclave/jar/sdk/host
  cp -r ${WORKDIR}/sdk/enclave/pom.xml ${WORKDIR}/release/opt/javaenclave/jar/sdk/enclave
  cp -r ${WORKDIR}/sdk/enclave/target/*.jar ${WORKDIR}/release/opt/javaenclave/jar/sdk/enclave
  cp -r ${WORKDIR}/sdk/common/pom.xml ${WORKDIR}/release/opt/javaenclave/jar/sdk/common
  cp -r ${WORKDIR}/sdk/common/target/*.jar ${WORKDIR}/release/opt/javaenclave/jar/sdk/common
  mkdir -p ${WORKDIR}/release/opt/javaenclave/jar/archetype
  cp -r ${WORKDIR}/archetype/pom.xml ${WORKDIR}/release/opt/javaenclave/jar/archetype
  cp -r ${WORKDIR}/archetype/target/*.jar ${WORKDIR}/release/opt/javaenclave/jar/archetype
  mkdir -p ${WORKDIR}/release/opt/javaenclave/jar/bouncycastle-native
  cp -r ${WORKDIR}/third-party-libs/bouncycastle-native/pom.xml ${WORKDIR}/release/opt/javaenclave/jar/bouncycastle-native
  cp -r ${WORKDIR}/third-party-libs/bouncycastle-native/target/*.jar ${WORKDIR}/release/opt/javaenclave/jar/bouncycastle-native
  build_release_image
}

function develop_javaenclave() {
  echo "develop javaenclave"
  build_base_image
  docker run -it --rm --privileged --network host                   \
  -w "${WORKDIR}"                                                   \
  -v "${HOME}"/.m2:/root/.m2 -v "${WORKDIR}":"${WORKDIR}"           \
  -e PCCS_URL=${PCCS_URL}                                           \
  -v /dev/sgx_enclave:/dev/sgx/enclave             \
  -v /dev/sgx_provision:/dev/sgx/provision         \
  ${BASE_IMAGE}:${BASE_TAG} /bin/bash
}

function develop_application() {
  echo "develop application based on JavaEnclave"
  build_release_image
  docker run -it --rm --privileged --network host                   \
  -w "${WORKDIR}"                                                   \
  -v "${HOME}"/.m2:/root/.m2 -v "${WORKDIR}":"${WORKDIR}"           \
  -e PCCS_URL=${PCCS_URL}                                           \
  -v /dev/sgx_enclave:/dev/sgx/enclave             \
  -v /dev/sgx_provision:/dev/sgx/provision         \
  ${RELEASE_IMAGE}:${RELEASE_TAG} /bin/bash
}

function clean_javaenclave() {
  echo "clean javaenclave"
  pushd ${WORKDIR}
  # remove all files generated in building and developing.
  # remove all target dir.
  find -name target | xargs rm -rf
  # remove all .o and .so files
  find -name *.o | xargs rm -rf && find -name *.so | xargs rm -rf
  # remove release dir.
  rm -rf ${WORKDIR}/release
  popd
}

if [ ! "$STAGE" ]; then
  # docker build javaenclave base image.
  # build JavaEnclave in javaenclave base image docker.
  # test JavaEnclave unit test case in javaenclave release image docker.
  build_javaenclave
  collect_javaenclave_release
  test_javaenclave
elif [ "build" = "$STAGE" ]; then
  # docker build javaenclave base image.
  build_javaenclave
elif [ "release" = "$STAGE" ]; then
  # docker build javaenclave release image.
  collect_javaenclave_release
elif [ "test" = "$STAGE" ]; then
  # test JavaEnclave unit test case in javaenclave release image docker.
  test_javaenclave
elif [ "samples" = "$STAGE" ]; then
  # run samples in javaenclave release image docker.
  samples_javaenclave
elif [ "benchmark" = "$STAGE" ]; then
  # run benchmark in javaenclave release image docker.
  benchmark_javaenclave
elif [ "develop" = "$STAGE" ]; then
  # enter javaenclave base image docker and develop JavaEnclave.
  develop_javaenclave
elif [ "develop_app" = "$STAGE" ]; then
  # enter javaenclave release image docker and develop application.
  develop_application
elif [ "clean" = "$STAGE" ]; then
  # remove all tmp files generated in build.
  clean_javaenclave
fi
