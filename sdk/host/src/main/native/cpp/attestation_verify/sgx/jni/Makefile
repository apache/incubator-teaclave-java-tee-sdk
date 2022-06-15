# Copyright (c)

include $(NATIVE_BASE_DIR)/config/config.mk
include $(NATIVE_BASE_DIR)/config/remote_attestation_verify/sgx/config.mk

.PHONY: all build clean

all: build

build:
	$(CC) -g -c -Wno-unused-parameter $(RA_VERIFY_INCDIR) -I$(JAVA_HOME)/lib -I$(JAVA_HOME)/include \
	-I$(JAVA_HOME)/include/$(shell uname -s | tr A-Z a-z) -fPIC jni_remote_attestation_verify.c
	$(CC) jni_remote_attestation_verify.o $(RA_VERIFY_LDFLAGS) -fPIC -shared -o $(BIN)/remote_attestation/sgx/jni/lib_jni_sgx_remote_attestation_verify.so

clean:
	rm -rf *.o
	rm -rf $(BIN)/remote_attestation/sgx/jni/lib_jni_sgx_remote_attestation_verify.so