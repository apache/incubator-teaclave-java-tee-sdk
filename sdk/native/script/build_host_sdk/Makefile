# Copyright (c)

.PHONY: all build clean

all: build

build:
ifeq ($(MOCK_IN_SVM), TRUE)
	$(MAKE) -C $(HOST_BASE_DIR)/src/main/native/cpp/platform/mock_in_svm/jni
endif

ifeq ($(TEE_SDK), TRUE)
	$(MAKE) -C $(HOST_BASE_DIR)/src/main/native/cpp/platform/tee_sdk_svm
endif

clean:
ifeq ($(MOCK_IN_SVM), TRUE)
	$(MAKE) -C $(HOST_BASE_DIR)/src/main/native/cpp/platform/mock_in_svm/jni clean
endif

ifeq ($(TEE_SDK), TRUE)
	$(MAKE) -C $(HOST_BASE_DIR)/src/main/native/cpp/platform/tee_sdk_svm clean
endif