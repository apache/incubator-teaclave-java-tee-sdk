# Copyright (c)

include $(NATIVE_BASE_DIR)/config/config.mk
include $(NATIVE_BASE_DIR)/config/platform/tee_sdk_svm/jni/config.mk

.PHONY: all build clean

all: build

build: jni.o
	$(CC) edge_routines/ocall_svm.o edge_routines/tee_sdk_enclave_u.o jni/jni_tee_sdk_svm.o \
	$(TS_HOST_CFLAGS) $(TS_HOST_LDFLAGS) -fPIC -shared -o $(BIN)/platform/tee_sdk_svm/jni/lib_jni_tee_sdk_svm.so
	
	rm -rf edge_routines/*.o edge_routines/tee_sdk_enclave_u.* jni/*.o

edge_routines.o:
	$(MAKE) -C edge_routines

jni.o: edge_routines.o
	$(MAKE) -C jni

clean:
	$(MAKE) -C edge_routines clean
	$(MAKE) -C jni clean