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

CC = gcc
CXX = g++

TEE_SDK_PATH = /opt/teesdk/sgxsdk
UBUNTU_OS = $(shell if [ -d "/usr/lib/x86_64-linux-gnu" ]; then echo "yes"; else echo "no"; fi;)
ifeq ("$(UBUNTU_OS)", "yes")
    DCAP_LIB_PATH = /usr/lib/x86_64-linux-gnu
else
    DCAP_LIB_PATH = /usr/lib64
endif

RA_VERIFY_INCDIR = -I$(TEE_SDK_PATH)/include
RA_VERIFY_LDFLAGS = -L$(DCAP_LIB_PATH) -lsgx_dcap_quoteverify -lsgx_dcap_ql
