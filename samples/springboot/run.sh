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

# step1: build springboot application service.
mvn -Pnative clean package

# step2: startup springboot application service.
OCCLUM_RELEASE_ENCLAVE=true $JAVA_HOME/bin/java -cp host/target/host-1.0-SNAPSHOT-jar-with-dependencies.jar:enclave/target/enclave-1.0-SNAPSHOT-jar-with-dependencies.jar org.apache.teaclave.javasdk.samples.springboot.host.Application &
sleep 5
# shellcheck disable=SC2006
# shellcheck disable=SC2009
PID=`ps -ef | grep "Application" | grep -v grep | awk '{print $2}'`

# step3: encrypt and decrypt service.
wget http://localhost:8080/enclaveEncDecService?data=SELECT+TOP+50+PERCENT+*+FROM+Websites;

# step4: digest service.
wget http://localhost:8080/enclaveDigestService?data=SELECT+TOP+50+PERCENT+*+FROM+Websites;

# step5: kill springboot service.
kill -9 "$PID"