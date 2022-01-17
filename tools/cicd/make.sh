#!/bin/bash

BUILD_IMAGE=javaenclave_build
BUILD_TAG=v0.1.0

SHELL_FOLDER=$(cd "$(dirname "$0")";pwd)

cd "${SHELL_FOLDER}"

WORKDIR=$(dirname $(dirname "$PWD"))

# check target images exist or not, build it if not.
if [[ "$(docker images -q ${BUILD_IMAGE}:${BUILD_TAG} 2> /dev/null)" == "" ]]; then
  docker build -t ${BUILD_IMAGE}:${BUILD_TAG} .
fi

# test JavaEnclave's unit test cases and samples
docker run -i --rm --privileged --network host                    \
-w "${WORKDIR}"                                                   \
-v "${HOME}"/.m2:/root/.m2 -v "${WORKDIR}":"${WORKDIR}"           \
${BUILD_IMAGE}:${BUILD_TAG} /bin/bash build.sh