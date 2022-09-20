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

# SGX_MODE ?= SIM
SGX_MODE ?= HW
SGX_ARCH ?= x64
SGX_DEBUG ?= 1
SGX_PCL ?= 1

SGX_COMMON_FLAGS := -m64
SGX_LIBRARY_PATH := $(TEE_SDK_PATH)/lib64
SGX_ENCLAVE_SIGNER := $(TEE_SDK_PATH)/bin/x64/sgx_sign
SGX_EDGER8R := $(TEE_SDK_PATH)/bin/x64/sgx_edger8r

ifeq ($(SGX_DEBUG), 1)
ifeq ($(SGX_PRERELEASE), 1)
	$(error Cannot set SGX_DEBUG and SGX_PRERELEASE at the same time!!)
endif
endif

ifeq ($(SGX_DEBUG), 1)
	SGX_COMMON_FLAGS += -O0 -g
	Encryption_Tool_Flags := -d
else
	SGX_COMMON_FLAGS += -O2
endif

ifneq ($(SGX_MODE), HW)
	Trts_Library_Name := sgx_trts_sim
	Urts_Library_Name := sgx_urts_sim
	Service_Library_Name := sgx_tservice_sim
else
	Trts_Library_Name := sgx_trts
	Urts_Library_Name := sgx_urts
	Service_Library_Name := sgx_tservice
endif

SGX_COMMON_FLAGS += -Wall -Wextra -Winit-self -Wpointer-arith -Wreturn-type \
                    -Waddress -Wsequence-point -Wformat-security            \
                    -Wmissing-include-dirs -Wfloat-equal -Wundef -Wshadow   \
                    -Wcast-align -Wcast-qual -Wconversion -Wredundant-decls

SGX_COMMON_CFLAGS := $(SGX_COMMON_FLAGS) -Wjump-misses-init -Wstrict-prototypes -Wunsuffixed-float-constants -std=c99
SGX_COMMON_CXXFLAGS := $(SGX_COMMON_FLAGS) -Wnon-virtual-dtor -std=c++11

TS_HOST_INCDIR = -I$(TEE_SDK_PATH)/include
TS_HOST_CFLAGS = $(TS_HOST_INCDIR) $(SGX_COMMON_CFLAGS)
TS_HOST_CXXFLAGS = $(SGX_COMMON_CXXFLAGS)
TS_HOST_LDFLAGS = -L$(SGX_LIBRARY_PATH) -L$(DCAP_LIB_PATH) -Wl,-z,noexecstack -lc -l$(Urts_Library_Name) -lpthread -lsgx_ustdc_ex -lsgx_dcap_quoteverify -lsgx_dcap_ql -lsgx_quote_ex

Enclave_Security_Link_Flags = -Wl,-z,relro,-z,now,-z,noexecstack

TS_ENCLAVE_INCDIR = -I$(TEE_SDK_PATH)/include -I$(TEE_SDK_PATH)/include/tlibc -I$(TEE_SDK_PATH)/include/libcxx -I$(TEE_SDK_PATH)/include/syscall
TS_ENCLAVE_CFLAGS = $(TS_ENCLAVE_INCDIR) -nostdinc -fvisibility=hidden -fpie -ffunction-sections -fdata-sections -fstack-protector-strong
TS_ENCLAVE_CXXFLAGS = $(TS_ENCLAVE_CFLAGS) -nostdinc++
TS_ENCLAVE_LDFLAGS = -L$(SGX_LIBRARY_PATH) $(TS_ENCLAVE_CFLAGS) -Wl,--no-undefined -nostdlib -nodefaultlibs -nostartfiles $(Enclave_Security_Link_Flags) \
				-Wl,--whole-archive -l$(Trts_Library_Name) -Wl,--no-whole-archive \
				-Wl,--start-group -lsgx_tstdc -lsgx_tstdc_ex -lsgx_tcxx -lsgx_pthread -lsgx_tcrypto -l$(Service_Library_Name)  -Wl,--end-group \
				-Wl,-Bstatic -Wl,-Bsymbolic -Wl,--no-undefined \
				-Wl,-pie,-eenclave_entry -Wl,--export-dynamic  \
				-Wl,--defsym,__ImageBase=0
