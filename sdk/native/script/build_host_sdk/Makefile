# Copyright (c)

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