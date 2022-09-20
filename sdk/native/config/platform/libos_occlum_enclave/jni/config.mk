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

CC := gcc
OCCLUM_PREFIX ?= /opt/occlum
SGX_MODE ?= HW

ifneq ($(SGX_MODE), HW)
	URTS_LIBRARY_NAME := sgx_urts_sim
	UAE_SERVICE_LIBRARY_NAME := sgx_uae_service_sim
	OCCLUM_PAL_LIB := occlum-pal_sim
else
	URTS_LIBRARY_NAME := sgx_urts
	UAE_SERVICE_LIBRARY_NAME := sgx_uae_service
	OCCLUM_PAL_LIB := occlum-pal
endif

C_FLAGS := -Wl,-z,noexecstack -g -c -Wno-unused-parameter -I$(OCCLUM_PREFIX)/include

LINK_FLAGS := -lpthread -L$(OCCLUM_PREFIX)/build/lib -L/opt/teesdk/sgxsdk/lib64 -l$(URTS_LIBRARY_NAME) -l$(UAE_SERVICE_LIBRARY_NAME) -lsgx_uprotected_fs