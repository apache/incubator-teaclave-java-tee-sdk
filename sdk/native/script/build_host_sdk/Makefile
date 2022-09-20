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
ifeq ($(MOCK_IN_SVM), TRUE)
	$(MAKE) -C $(HOST_BASE_DIR)/src/main/native/cpp/platform/mock_in_svm/jni
endif

ifeq ($(TEE_SDK), TRUE)
	$(MAKE) -C $(HOST_BASE_DIR)/src/main/native/cpp/platform/tee_sdk_svm
	$(MAKE) -C $(HOST_BASE_DIR)/src/main/native/cpp/platform/libos_occlum_enclave/jni
endif

ifeq ($(EMBEDDED_LIB_OS), TRUE)
	$(MAKE) -C $(HOST_BASE_DIR)/src/main/native/cpp/platform/libos_occlum_enclave/jni
endif

ifeq ($(TEE_SDK), TRUE)
	$(MAKE) -C $(HOST_BASE_DIR)/src/main/native/cpp/attestation_verify/sgx/jni
else ifeq ($(EMBEDDED_LIB_OS), TRUE)
	$(MAKE) -C $(HOST_BASE_DIR)/src/main/native/cpp/attestation_verify/sgx/jni
endif

clean:
ifeq ($(MOCK_IN_SVM), TRUE)
	$(MAKE) -C $(HOST_BASE_DIR)/src/main/native/cpp/platform/mock_in_svm/jni clean
endif

ifeq ($(TEE_SDK), TRUE)
	$(MAKE) -C $(HOST_BASE_DIR)/src/main/native/cpp/platform/tee_sdk_svm clean
endif

ifeq ($(EMBEDDED_LIB_OS), TRUE)
	$(MAKE) -C $(HOST_BASE_DIR)/src/main/native/cpp/platform/libos_occlum_enclave/jni clean
endif

ifeq ($(TEE_SDK), TRUE)
	$(MAKE) -C $(HOST_BASE_DIR)/src/main/native/cpp/attestation_verify/sgx/jni clean
else ifeq ($(EMBEDDED_LIB_OS), TRUE)
	$(MAKE) -C $(HOST_BASE_DIR)/src/main/native/cpp/attestation_verify/sgx/jni clean
endif