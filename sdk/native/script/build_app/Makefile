include /opt/javaenclave/config/config.mk
include /opt/javaenclave/config/platform/tee_sdk_svm/jni/config.mk

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

	$(CC) -o ${ENCLAVE_BASE_DIR}/target/svm-output/lib_tee_sdk_svm_load \
	/opt/javaenclave/bin/platform/tee_sdk_svm/*.o ${ENCLAVE_BASE_DIR}/target/svm-output/lib_tee_sdk_svm_load.o \
	-L${JAVA_HOME}/lib/static/linux-amd64/glibc    \
	-L${ENCLAVE_BASE_DIR}/target/enclave_workspace \
	-fpie -ljava -lzip -lnio -lnet -ljvm -lfdlibm -llibchelper \
	$(TS_ENCLAVE_LDFLAGS) -Wl,--version-script=/opt/javaenclave/config/platform/tee_sdk_svm/edl/tee_sdk_enclave.lds

    # sign the enclave image
    ifeq ($(ENCLAVE_PRIVATE_PEM_PATH), )
		openssl genrsa -out ${ENCLAVE_BASE_DIR}/target/enclave_workspace/private.pem -3 3072
		$(SGX_ENCLAVE_SIGNER) sign -enclave ${ENCLAVE_BASE_DIR}/target/svm-output/lib_tee_sdk_svm_load \
		-out ${ENCLAVE_BASE_DIR}/target/svm-output/lib_tee_sdk_svm_load.signed \
		-config ${ENCLAVE_BASE_DIR}/src/main/resources/tee_sdk_svm.conf -key ${ENCLAVE_BASE_DIR}/target/enclave_workspace/private.pem
    else
		$(SGX_ENCLAVE_SIGNER) sign -enclave ${ENCLAVE_BASE_DIR}/target/svm-output/lib_tee_sdk_svm_load \
        -out ${ENCLAVE_BASE_DIR}/target/svm-output/lib_tee_sdk_svm_load.signed \
        -config ${ENCLAVE_BASE_DIR}/src/main/resources/tee_sdk_svm.conf -key $(ENCLAVE_PRIVATE_PEM_PATH)
    endif
endif
