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

.PHONY: all build clean

all: build

build:
ifeq ($(TEE_SDK), TRUE)
	$(MAKE) -C $(ENCLAVE_BASE_DIR)/src/main/native/cpp/platform/tee_sdk_svm/wrapper
	$(MAKE) -C $(ENCLAVE_BASE_DIR)/src/main/native/cpp/platform/tee_sdk_svm/edge_routines
	$(MAKE) -C $(ENCLAVE_BASE_DIR)/src/main/native/cpp/platform/tee_sdk_svm/remote_attestation_generate
endif

ifeq ($(EMBEDDED_LIB_OS), TRUE)
	$(MAKE) -C $(ENCLAVE_BASE_DIR)/src/main/native/cpp/platform/libos_occlum_enclave/remote_attestation_generate
endif

clean:
ifeq ($(TEE_SDK), TRUE)
	$(MAKE) -C $(ENCLAVE_BASE_DIR)/src/main/native/cpp/platform/tee_sdk_svm/wrapper clean
	$(MAKE) -C $(ENCLAVE_BASE_DIR)/src/main/native/cpp/platform/tee_sdk_svm/edge_routines clean
	$(MAKE) -C $(ENCLAVE_BASE_DIR)/src/main/native/cpp/platform/tee_sdk_svm/remote_attestation_generate clean
endif

ifeq ($(EMBEDDED_LIB_OS), TRUE)
	$(MAKE) -C $(ENCLAVE_BASE_DIR)/src/main/native/cpp/platform/libos_occlum_enclave/remote_attestation_generate clean
endif