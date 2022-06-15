include $(NATIVE_BASE_DIR)/config/config.mk
include $(NATIVE_BASE_DIR)/config/platform/tee_sdk_svm/jni/config.mk

.PHONY: all build clean

all: build

build:
	$(CC) -g -c -I$(INCLUDE) -I$(JAVA_HOME)/lib $(TS_ENCLAVE_INCDIR) $(TS_ENCLAVE_CFLAGS) generate_attestation_report.c \
    -o $(BIN)/platform/tee_sdk_svm/generate_attestation_report.o

clean:
	rm -rf $(BIN)/platform/tee_sdk_svm/generate_attestation_report.o