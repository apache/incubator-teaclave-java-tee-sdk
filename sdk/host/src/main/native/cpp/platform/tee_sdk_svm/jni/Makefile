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
	$(CC) -g -c -Wno-unused-parameter -fPIC $(TS_HOST_CFLAGS) $(TS_HOST_INCDIR) -I./../edge_routines -I$(JAVA_HOME)/lib -I$(INCLUDE) -I$(JAVA_HOME)/include \
	-I$(JAVA_HOME)/include/$(shell uname -s | tr A-Z a-z) jni_tee_sdk_svm.c

	$(CC) -g -c -Wno-unused-parameter -fPIC $(TS_HOST_CFLAGS) $(TS_HOST_INCDIR) -I$(JAVA_HOME)/lib -I$(INCLUDE) -I$(JAVA_HOME)/include \
	-I$(JAVA_HOME)/include/$(shell uname -s | tr A-Z a-z) generate_attestation_report.c

clean:
	rm -rf *.o