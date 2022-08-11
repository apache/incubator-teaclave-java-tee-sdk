#/bin/bash

mvn --settings /root/tools/settings.xml -Pnative clean package

OCCLUM_RELEASE_ENCLAVE=true java -cp host/target/host-1.0-SNAPSHOT-jar-with-dependencies.jar:enclave/target/enclave-1.0-SNAPSHOT-jar-with-dependencies.jar com.alibaba.samples.helloworld.host.Main
