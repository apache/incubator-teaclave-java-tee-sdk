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
include $(NATIVE_BASE_DIR)/config/platform/tee_sdk_svm/jni/config.mk

.PHONY: all build clean

all: build

build:
	$(SGX_EDGER8R) $(CONFIG)/platform/tee_sdk_svm/edl/tee_sdk_enclave.edl --trusted --search-path $(TEE_SDK_PATH)/include
	$(CC) -g -c -std=c99 -I$(INCLUDE) $(TS_ENCLAVE_INCDIR) $(TS_ENCLAVE_CFLAGS) tee_sdk_enclave_t.c sgx_mmap.c tee_sdk_symbol.c

	mv *.o $(BIN)/platform/tee_sdk_svm/

	rm -rf tee_sdk_enclave_t.*

clean:
	rm -rf $(BIN)/platform/tee_sdk_svm/wrapper/*.o
	rm -rf tee_sdk_svm_*