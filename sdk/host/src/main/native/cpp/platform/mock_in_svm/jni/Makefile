# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

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