#!/bin/bash
BC_NATIVE_HOME=$(pushd $(dirname $0) > /dev/null && pwd && popd > /dev/null)
BC_HOME=$1
BC_VERSION=$2

# Check BouncyCastle source directory
if [ ! -d $BC_HOME ];then
  echo "Specified BouncyCastle source root $BC_HOME doesn't exist. Please make sure the directory is correct."
  exit 1
fi

# Check specified BouncyCastle version
if [ $BC_VERSION"x" != "1.66x" -a $BC_VERSION"x" != "1.70x" ];then
  echo "BouncyCastle version $BC_VERSION is not supported. Currently supported are 1.66, 1.70"
  exit 1
fi

# Check if GraalVM is set.
java -version 2>&1 | sed '2!d' | grep GraalVM
if [ $? == 1 ];then
  $GRAALVM_HOME/bin/java -version 2>&1 | sed '2!d' | grep GraalVM
  if [ $? == 0 ];then
    GRADLE_JAVA_HOME="-Dorg.gradle.java.home=$GRAALVM_HOME"
  else
    echo "Can't find GraalVM JDK installed. Please install GraalVM JDK and set it as default java, or set GRAALVM_HOME variable to its home"
    exit 1
  fi
fi

BC_TAG="r1rv"${BC_VERSION#*.}

pushd $BC_HOME > /dev/null
#Assume the $BC_HOME is cloned from https://github.com/bcgit/bc-java
git checkout $BC_TAG -b $BC_TAG
#Copy the agent filter files
cp $BC_NATIVE_HOME/bc-java/*.json .
if [ "$BC_VERSION""x" == "1.70x" ];then
  git apply $BC_NATIVE_HOME/bc-java/1.70.build.gradle.patch
elif [ "$BC_VERSION""x" == "1.66x" ];then
  # This patch is for 1.66 and Gradle 7
  git apply $BC_NATIVE_HOME/bc-java/1.66.gradle7.build.gradle.patch
fi

echo "Start BouncyCastle tests to collect native image configurations. It may take a while."
gradle $GRADLE_JAVA_HOME test

BASE_MODULES="pg prov pkix core tls mail"
if [ $(echo "${BC_VERSION} < 1.69" | bc) = 1 ];then
  MODULES=$BASE_MODULES
else
  # util module is added since 1.69
  MODULES="$BASE_MODULES util"
fi

for module in $MODULES ;
do
  CONFIG_DIR=$BC_NATIVE_HOME/src/main/resources/configs/$BC_VERSION/$module
  if [ ! -d $CONFIG_DIR ];then
    mkdir -p $CONFIG_DIR
  fi
  cp $module/test-configs/* $CONFIG_DIR/ 
done
popd > /dev/null

pushd $BC_NATIVE_HOME > /dev/null
#Check if there is any test class recorded in the configuration files
TESTS_IN_CONFIG=$(grep org.bouncycastle.*Tests . -rn --include="*config.json")
if [ "$TESTS_IN_CONFIG""x" != "x" ];then
  echo -e "Need to delete the following test classes from configurations:\n"
  echo -e "$TESTS_IN_CONFIG\n"
  exit 1
fi
popd > /dev/null
