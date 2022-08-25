#!/bin/bash

STAGE=$1

# set sgx enclave remote attestation PCCS_URL.
echo "PCCS_URL=${PCCS_URL}" > /etc/sgx_default_qcnl.conf
echo "USE_SECURE_CERT=TRUE" >> /etc/sgx_default_qcnl.conf

# parse shell file's path location.
SHELL_FOLDER=$(cd "$(dirname "$0")";pwd)
cd "${SHELL_FOLDER}"

# workspace dir is the same as build.sh path location.
WORKDIR="$PWD"
SETTING="--settings /root/tools/settings.xml"

if [ ! "$STAGE" -o "build" = "$STAGE" ]; then
  pushd "${WORKDIR}"/sdk && mvn ${SETTING} clean install && popd
  # Install BouncyCastle Native Package
  pushd "${WORKDIR}"/third-party-libs/bouncycastle-native && mvn $SETTING clean install && popd
  # Install JavaEnclave archetype
  pushd "${WORKDIR}"/archetype && mvn $SETTING clean install && popd
elif [ ! "$STAGE" -o "test" = "$STAGE" ]; then
  # Test unit test cases in JavaEnclave
  pushd "${WORKDIR}"/test && OCCLUM_RELEASE_ENCLAVE=true mvn $SETTING -Pnative clean package && popd
elif [ ! "$STAGE" -o "samples" = "$STAGE" ]; then
  # samples in JavaEnclave
  pushd "${WORKDIR}"/samples/helloworld && ./run.sh && popd
  pushd "${WORKDIR}"/samples/springboot && ./run.sh && popd
elif [ ! "$STAGE" -o "benchmark" = "$STAGE" ]; then
  # benchmark in JavaEnclave
  pushd "${WORKDIR}"/benchmark/guomi && ./run.sh && popd
  pushd "${WORKDIR}"/benchmark/string && ./run.sh && popd
fi
