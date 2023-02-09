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

# Setting PCCS_URL for SGX Remote Attestation.
echo "PCCS_URL=https://sgx-dcap-server.cn-beijing.aliyuncs.com/sgx/certification/v3/" > /etc/sgx_default_qcnl.conf
echo "USE_SECURE_CERT=TRUE" >> /etc/sgx_default_qcnl.conf

TEST_PATH=$1

# Download jacoco agent from maven central repo.
rm -rf jacoco && mkdir -p jacoco && rm -rf result && mkdir -p result/classes && rm -rf site
pushd jacoco
wget https://search.maven.org/remotecontent?filepath=org/jacoco/jacoco/0.8.3/jacoco-0.8.3.zip -O temp.zip && unzip temp.zip && rm -rf temp.zip
popd

# Generate Teaclave java sdk test coverage data.
OCCLUM_RELEASE_ENCLAVE=true $JAVA_HOME/bin/java -javaagent:jacoco/lib/jacocoagent.jar=destfile=./result/jacoco.exec,append=true,classdumpdir=result/classes,includes=org.apache.teaclave.javasdk.host.*:org.apache.teaclave.javasdk.common.*:org.apache.teaclave.javasdk.enclave.*,output=file -cp ${TEST_PATH}/host/target/host-0.1.0-jar-with-dependencies.jar:${TEST_PATH}enclave/target/enclave-0.1.0-jar-with-dependencies.jar org.apache.teaclave.javasdk.test.host.TestMain

# Generate Teaclave java sdk test coverage report.
$JAVA_HOME/bin/java -jar jacoco/lib/jacococli.jar report result/jacoco.exec --classfiles result/classes --html site