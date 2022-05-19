# Copyright (c)

include $(NATIVE_BASE_DIR)/config/config.mk
include $(NATIVE_BASE_DIR)/config/platform/tee_sdk_svm/jni/config.mk

.PHONY: all build clean

all: build

build:
	$(SGX_EDGER8R) $(CONFIG)/platform/tee_sdk_svm/edl/tee_sdk_enclave.edl --trusted --search-path $(TEE_SDK_PATH)/include
	$(CC) -g -c -std=c99 -I$(INCLUDE) $(TS_ENCLAVE_INCDIR) $(TS_ENCLAVE_CFLAGS) tee_sdk_enclave_t.c sgx_mmap.c tee_sdk_symbol.c

	mv *.o $(BIN)/platform/tee_sdk_svm/

	rm -rf tee_sdk_enclave_t.*

clean:
	rm -rf $(BIN)/platform/tee_sdk_svm/wrapper/*.o
	rm -rf tee_sdk_svm_*