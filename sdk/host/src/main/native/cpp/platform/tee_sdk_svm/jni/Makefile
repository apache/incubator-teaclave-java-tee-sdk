# Copyright (c)

include $(NATIVE_BASE_DIR)/config/config.mk
include $(NATIVE_BASE_DIR)/config/platform/tee_sdk_svm/jni/config.mk

.PHONY: all build clean

all: build

build:
	$(CC) -g -c -Wno-unused-parameter -fPIC $(TS_HOST_CFLAGS) $(TS_HOST_INCDIR) -I./../edge_routines -I$(JAVA_HOME)/lib -I$(INCLUDE) -I$(JAVA_HOME)/include \
	-I$(JAVA_HOME)/include/$(shell uname -s | tr A-Z a-z) jni_tee_sdk_svm.c

	$(CC) -g -c -Wno-unused-parameter -fPIC $(TS_HOST_CFLAGS) $(TS_HOST_INCDIR) -I$(JAVA_HOME)/lib -I$(INCLUDE) -I$(JAVA_HOME)/include \
	-I$(JAVA_HOME)/include/$(shell uname -s | tr A-Z a-z) generate_attestation_report.c

clean:
	rm -rf *.o