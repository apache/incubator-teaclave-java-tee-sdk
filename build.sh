#!/bin/bash

# parse shell file's path location.
SHELL_FOLDER=$(cd "$(dirname "$0")";pwd)

cd "${SHELL_FOLDER}"

# workspace dir is the same as build.sh path location.
WORKDIR="$PWD"
SETTING="--settings /root/tools/settings.xml"

# Build JavaEnclave SDK
cd "${WORKDIR}"/sdk && mvn $SETTING clean install
# Install JavaEnclave SDK
rm -rf /opt/javaenclave && mkdir -p /opt/javaenclave && cp -r ${SHELL_FOLDER}/sdk/native/bin /opt/javaenclave \
&& cp -r ${SHELL_FOLDER}/sdk/native/config /opt/javaenclave && cp -r ${SHELL_FOLDER}/sdk/native/script/build_app /opt/javaenclave
# Test unit test cases in JavaEnclave
cd "${WORKDIR}"/test && mvn -X $SETTING -Pnative -e clean package
