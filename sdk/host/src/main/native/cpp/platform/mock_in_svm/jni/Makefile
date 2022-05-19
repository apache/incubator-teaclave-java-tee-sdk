include $(NATIVE_BASE_DIR)/config/config.mk
include $(NATIVE_BASE_DIR)/config/platform/mock_in_svm/jni/config.mk

.PHONY: all build clean

all: build

# compile jni_mock_in_svm.c to $(BIN)/platform/mock_in_svm/jni/lib_jni_mock_svm.so
build:
	$(CC) jni_mock_in_svm.c -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/$(shell uname -s | tr A-Z a-z) \
	-I$(JAVA_HOME)/lib $(DB_LDFLAGS) -I$(INCLUDE) -fPIC -shared -o $(BIN)/platform/mock_in_svm/jni/lib_jni_mock_svm.so

# clean lib_jni_mock_svm.so
clean:
	rm -rf *.o $(BIN)/platform/mock_in_svm/jni/lib_jni_mock_svm.so