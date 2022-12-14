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

include /opt/javaenclave/config/config.mk
include /opt/javaenclave/config/platform/tee_sdk_svm/jni/config.mk

LIB_SUNEC=sunec

.PHONY: all build clean

all:
	$(MAKE) build

build:
ifeq ($(TEE_SDK), TRUE)
    # remove lib_tee_sdk_svm_load.so from target/svm-output, because it's
    # compiled target to common execution environment.
	rm -rf ${ENCLAVE_BASE_DIR}/target/svm-output/lib_tee_sdk_svm_load.so

    # copy part of dependency underlying libs to enclave_workspace
	cp ${JAVA_HOME}/languages/nfi/builder/clibraries-libffi/libffi.a       ${ENCLAVE_BASE_DIR}/target/enclave_workspace
	cp ${JAVA_HOME}/lib/svm/builder/clibraries/linux-amd64/libjvm.a        ${ENCLAVE_BASE_DIR}/target/enclave_workspace
	cp ${JAVA_HOME}/lib/svm/builder/clibraries/linux-amd64/liblibchelper.a ${ENCLAVE_BASE_DIR}/target/enclave_workspace

	# copy libenc_sunec.a from svm-output to enclave_workspace if it exist.
	cp ${ENCLAVE_BASE_DIR}/target/svm-output/libenc_sunec.a ${ENCLAVE_BASE_DIR}/target/enclave_workspace 2>/dev/null || :

    ifneq ($(wildcard ${ENCLAVE_BASE_DIR}/target/enclave_workspace/libenc_sunec.a),)
		LIB_SUNEC=enc_sunec
    endif

	$(CC) -o ${ENCLAVE_BASE_DIR}/target/svm-output/lib_tee_sdk_svm_load \
	/opt/javaenclave/bin/platform/tee_sdk_svm/*.o ${ENCLAVE_BASE_DIR}/target/svm-output/lib_tee_sdk_svm_load.o \
	-L${JAVA_HOME}/lib/static/linux-amd64/glibc    \
	-L${ENCLAVE_BASE_DIR}/target/enclave_workspace \
	-fpie -ljava -lzip -lnio -lnet -l$(LIB_SUNEC) -ljvm -lfdlibm -llibchelper \
	$(TS_ENCLAVE_LDFLAGS) -Wl,--version-script=/opt/javaenclave/config/platform/tee_sdk_svm/edl/tee_sdk_enclave.lds

    # create tee_sdk.conf
	/bin/bash ${BUILD_SCRIPT_DIR}/create_tee_sdk_configure.sh ${ENCLAVE_BASE_DIR}/src/main/resources ${ENCLAVE_BASE_DIR}/target/enclave_workspace

    ifeq ($(ENCLAVE_PRIVATE_PEM_PATH), )
		openssl genrsa -out ${ENCLAVE_BASE_DIR}/target/enclave_workspace/private.pem -3 3072
		$(SGX_ENCLAVE_SIGNER) sign -enclave ${ENCLAVE_BASE_DIR}/target/svm-output/lib_tee_sdk_svm_load \
		-out ${ENCLAVE_BASE_DIR}/target/svm-output/lib_tee_sdk_svm_load.signed \
		-config ${ENCLAVE_BASE_DIR}/target/enclave_workspace/TeeSDK.conf -key ${ENCLAVE_BASE_DIR}/target/enclave_workspace/private.pem
    else
		$(SGX_ENCLAVE_SIGNER) sign -enclave ${ENCLAVE_BASE_DIR}/target/svm-output/lib_tee_sdk_svm_load \
        -out ${ENCLAVE_BASE_DIR}/target/svm-output/lib_tee_sdk_svm_load.signed \
        -config ${ENCLAVE_BASE_DIR}/target/enclave_workspace/TeeSDK.conf -key $(ENCLAVE_PRIVATE_PEM_PATH)
    endif
endif

ifeq ($(EMBEDDED_LIB_OS), TRUE)
	/bin/bash ${BUILD_SCRIPT_DIR}/libos_occlum_enclave_build.sh
endif
