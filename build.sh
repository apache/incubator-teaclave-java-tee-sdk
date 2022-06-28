#!/bin/bash

# set sgx enclave remote attestation PCCS_URL.
echo "PCCS_URL=${PCCS_URL}" > /etc/sgx_default_qcnl.conf
echo "USE_SECURE_CERT=TRUE" >> /etc/sgx_default_qcnl.conf

# parse shell file's path location.
SHELL_FOLDER=$(cd "$(dirname "$0")";pwd)

cd "${SHELL_FOLDER}"

# fix occlum aesm service issue.
sed -i '128,129s/.*//g' /opt/occlum/build/bin/occlum

# workspace dir is the same as build.sh path location.
WORKDIR="$PWD"
SETTING="--settings /root/tools/settings.xml"

# Build JavaEnclave SDK
cd "${WORKDIR}"/sdk && mvn $SETTING clean install
# Install JavaEnclave SDK
rm -rf /opt/javaenclave && mkdir -p /opt/javaenclave && cp -r ${SHELL_FOLDER}/sdk/native/bin /opt/javaenclave \
&& cp -r ${SHELL_FOLDER}/sdk/native/config /opt/javaenclave && cp -r ${SHELL_FOLDER}/sdk/native/script/build_app /opt/javaenclave
# Test unit test cases in JavaEnclave
cd "${WORKDIR}"/test && OCCLUM_RELEASE_ENCLAVE=true mvn $SETTING -Pnative clean package
