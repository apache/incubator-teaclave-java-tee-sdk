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

rpm --import /etc/pki/rpm-gpg/RPM-GPG-KEY*
dnf clean all && rm -r /var/cache/dnf
dnf group install 'Development Tools'
dnf --enablerepo=PowerTools install -y ocaml ocaml-ocamlbuild redhat-rpm-config openssl-devel wget rpm-build git cmake perl python2 gcc-c++
alternatives --set python /usr/bin/python2

rm -rf linux-sgx

git clone https://github.com/intel/linux-sgx.git

pushd linux-sgx && git checkout stdc_ex_1.0

make preparation && cp external/toolset/centos8/* /usr/local/bin && which ar as ld objcopy objdump ranlib

make sdk && make sdk_install_pkg && popd

cp linux-sgx/linux/installer/bin/sgx_linux_x64_sdk_*.bin ./

rm -rf linux-sgx

popd