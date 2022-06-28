include $(NATIVE_BASE_DIR)/config/config.mk
include $(NATIVE_BASE_DIR)/config/platform/libos_occlum_enclave/jni/config.mk

.PHONY: all build clean

all: build

build:
	$(CC) $(C_FLAGS) -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/$(shell uname -s | tr A-Z a-z) -fPIC jni_occlum_enclave.c
	$(CC) jni_occlum_enclave.o $(LINK_FLAGS) -fPIC -shared -o $(BIN)/platform/libos_occlum_enclave/jni/lib_jni_embedded_lib_os_enclave.so

clean:
	rm -rf *.o $(BIN)/platform/libos_occlum_enclave/jni/lib_jni_embedded_lib_os_enclave.so