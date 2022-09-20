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