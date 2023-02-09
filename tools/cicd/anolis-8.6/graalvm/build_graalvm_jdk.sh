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

SHELL_FOLDER=$(cd "$(dirname "$0")";pwd)
pushd "${SHELL_FOLDER}"

dnf clean all && rm -r /var/cache/dnf && dnf --enablerepo=PowerTools install -y wget

# Download GraalVM_22.2.0 JDK from github
wget -c -q https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-22.2.0/graalvm-ce-java11-linux-amd64-22.2.0.tar.gz -O - | tar -xz

# cd graalvm-ce-java11-22.2.0 and gu install native-image
pushd graalvm-ce-java11-22.2.0 && ./bin/gu install native-image && popd

# archive graalvm-ce-java11-22.2.0 which installed native-image
tar -zcvf graalvm-ce-java11-22.2.0.tar.gz graalvm-ce-java11-22.2.0 && rm -rf graalvm-ce-java11-22.2.0

popd