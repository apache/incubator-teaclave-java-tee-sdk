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

# set sgx enclave remote attestation PCCS_URL.
echo "PCCS_URL=${PCCS_URL}" > /etc/sgx_default_qcnl.conf
echo "USE_SECURE_CERT=TRUE" >> /etc/sgx_default_qcnl.conf

# parse shell file's path location.
SHELL_FOLDER=$(cd "$(dirname "$0")";pwd)
cd "${SHELL_FOLDER}"

# workspace dir is the same as build.sh path location.
WORKDIR="$PWD"

if [ ! "$STAGE" -o "build" = "$STAGE" ]; then
  # Install local graal-processor.jar
  mvn install:install-file -DgroupId=org.graalvm.compiler -DartifactId=graal-processor -Dversion=22.2.0 -Dpackaging=jar -Dfile="${GRAALVM_HOME}"/lib/graal/graal-processor.jar
  # Build and Install Teaclave-java-tee-sdk.
  pushd "${WORKDIR}"/sdk && mvn clean install && popd
  # Install BouncyCastle Native Package
  pushd "${WORKDIR}"/third-party-libs/bouncycastle-native && mvn clean install && popd
  # Install JavaEnclave archetype
  pushd "${WORKDIR}"/archetype && mvn clean install && popd
elif [ ! "$STAGE" -o "test" = "$STAGE" ]; then
  # Test unit test cases in JavaEnclave
  pushd "${WORKDIR}"/test && ./run.sh && popd
elif [ ! "$STAGE" -o "coverage" = "$STAGE" ]; then
  # collect and analysis JavaEnclave ut coverage
  pushd "${WORKDIR}"/coverage && ./run.sh "${WORKDIR}"/test && popd
elif [ ! "$STAGE" -o "samples" = "$STAGE" ]; then
  # samples in JavaEnclave
  pushd "${WORKDIR}"/samples/helloworld && ./run.sh && popd
  pushd "${WORKDIR}"/samples/springboot && ./run.sh && popd
elif [ ! "$STAGE" -o "benchmark" = "$STAGE" ]; then
  # benchmark in JavaEnclave
  pushd "${WORKDIR}"/benchmark/guomi && ./run.sh && popd
  pushd "${WORKDIR}"/benchmark/string && ./run.sh && popd
fi