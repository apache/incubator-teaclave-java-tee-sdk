# Copyright (c)

.PHONY: all build clean

all: build

build:
ifeq ($(TEE_SDK), TRUE)
	$(MAKE) -C $(ENCLAVE_BASE_DIR)/src/main/native/cpp/platform/tee_sdk_svm/wrapper
	$(MAKE) -C $(ENCLAVE_BASE_DIR)/src/main/native/cpp/platform/tee_sdk_svm/edge_routines
endif

clean:
ifeq ($(TEE_SDK), TRUE)
	$(MAKE) -C $(ENCLAVE_BASE_DIR)/src/main/native/cpp/platform/tee_sdk_svm/wrapper clean
	$(MAKE) -C $(ENCLAVE_BASE_DIR)/src/main/native/cpp/platform/tee_sdk_svm/edge_routines clean
endif