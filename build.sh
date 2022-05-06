#!/bin/bash

# parse shell file's path location.
SHELL_FOLDER=$(cd "$(dirname "$0")";pwd)

cd "${SHELL_FOLDER}"

# workspace dir is the same as build.sh path location.
WORKDIR="$PWD"

# The necessary GraalVM jars are compiled from git@gitlab.alibaba-inc.com:graal/SGXGraalVM.git.
# When the patches are accepted by the community, these jars will be gradually replaced by the official jars.
VERSION="enclave-22.0.0"
mkdir jartmp
pushd jartmp > /dev/null
wget https://graal.oss-cn-beijing.aliyuncs.com/graal-enclave/JDK11-22.0.0/graal-sdk-enclave-22.0.0.jar

mvn install:install-file -Dfile=$GRAALVM_HOME/lib/graal/graal-processor.jar -DgroupId=org.graalvm.compiler -DartifactId=graal-processor -Dversion=$VERSION -Dpackaging=jar
mvn install:install-file -Dfile=graal-sdk-enclave-22.0.0.jar -DgroupId=org.graalvm.sdk -DartifactId=graal-sdk -Dversion=$VERSION -Dpackaging=jar
mvn install:install-file -Dfile=$GRAALVM_HOME/lib/svm/builder/svm.jar -DgroupId=org.graalvm.nativeimage -DartifactId=svm -Dversion=$VERSION -Dpackaging=jar
mvn install:install-file -Dfile=$GRAALVM_HOME/lib/svm/builder/objectfile.jar -DgroupId=org.graalvm.nativeimage -DartifactId=objectfile -Dversion=$VERSION -Dpackaging=jar
mvn install:install-file -Dfile=$GRAALVM_HOME/lib/svm/builder/pointsto.jar -DgroupId=org.graalvm.nativeimage -DartifactId=pointsto -Dversion=$VERSION -Dpackaging=jar
mvn install:install-file -Dfile=$GRAALVM_HOME/lib/svm/builder/native-image-base.jar -DgroupId=org.graalvm.nativeimage -DartifactId=native-image-base -Dversion=$VERSION -Dpackaging=jar

popd > /dev/null
rm -rf jartmp

cd "${WORKDIR}"/sdk && mvn clean install
cd "${WORKDIR}"/test && mvn -Pnative -e clean package
