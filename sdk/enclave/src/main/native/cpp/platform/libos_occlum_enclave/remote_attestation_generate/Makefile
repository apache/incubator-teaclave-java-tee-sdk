include $(NATIVE_BASE_DIR)/config/config.mk
include $(NATIVE_BASE_DIR)/config/platform/libos_occlum_enclave/enclave/config.mk

.PHONY: all build clean

all: build

build:
	$(CC) jni_occlum_attestation_generate.c -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/$(shell uname -s | tr A-Z a-z) \
	-I$(INCPATH) -I$(TEE_SDK_PATH)/include -L$(LIBPATH) -locclum_dcap -fPIC -shared \
	-o $(BIN)/platform/libos_occlum_enclave/libos_occlum_enclave_attestation/lib_occlum_attestation_generate.so

clean:
	rm -rf $(BIN)/platform/libos_occlum_enclave/libos_occlum_enclave_attestation/lib_occlum_attestation_generate.so