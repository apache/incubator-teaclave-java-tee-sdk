# Copyright (c)

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