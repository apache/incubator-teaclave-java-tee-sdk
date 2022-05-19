include $(NATIVE_BASE_DIR)/config/config.mk
include $(NATIVE_BASE_DIR)/config/platform/tee_sdk_svm/jni/config.mk

.PHONY: all build clean

all: build

build:
	$(CC) -g -c -I$(INCLUDE) -I$(JAVA_HOME)/lib $(TS_ENCLAVE_INCDIR) $(TS_ENCLAVE_CFLAGS) tee_sdk_wrapper.c \
    -o $(BIN)/platform/tee_sdk_svm/tee_sdk_wrapper.o

clean:
	rm -rf $(BIN)/platform/tee_sdk_svm/tee_sdk_wrapper.o