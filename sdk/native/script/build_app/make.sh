#!/bin/bash

# shellcheck disable=SC2006
this_script_dir=`dirname "$0"`

export ENCLAVE_BASE_DIR="$1"
enclave_platform_config=$2

# Create a native image building workspace in application's enclave submodule.
mkdir -p "${ENCLAVE_BASE_DIR}"/target/enclave_workspace
# copy Makefile script to enclave_workspace.
cp -r "${this_script_dir}"/Makefile "${ENCLAVE_BASE_DIR}"/target/enclave_workspace

# cd to enclave workspace.
cd "${ENCLAVE_BASE_DIR}"/target/enclave_workspace

# process supported enclave platform set
OLD_IFS="$IFS"
IFS=":"
enclave_platform_array=($enclave_platform_config)
IFS="$OLD_IFS"

# Setting TEE Platform for makefile build process.
# shellcheck disable=SC2068
for enclave_platform in ${enclave_platform_array[@]}
do
  echo "$enclave_platform"
  # set "enclave_platform" as TRUE to indicate how
  # to compile jni.so and edge routine
  export "$enclave_platform"=TRUE
done

make -f ./Makefile