# Copyright (c)

include $(NATIVE_BASE_DIR)/config/config.mk
include $(NATIVE_BASE_DIR)/config/platform/tee_sdk_svm/jni/config.mk

.PHONY: all build clean

all: build

build:
	$(SGX_EDGER8R) $(CONFIG)/platform/tee_sdk_svm/edl/tee_sdk_enclave.edl --untrusted \
	--search-path $(TEE_SDK_PATH)/include

	$(CC) -g -c -fPIC $(TS_HOST_INCDIR) $(TS_HOST_CFLAGS) -fPIC ocall_svm.c

	$(CC) -g -c -fPIC $(TS_HOST_INCDIR) $(TS_HOST_CFLAGS) -fPIC ocall_attestation.c

	$(CC) -g -c -fPIC $(TS_HOST_INCDIR) $(TS_HOST_CFLAGS) -fPIC tee_sdk_enclave_u.c

clean:
	rm -rf tee_sdk_enclave_* *.o