#/bin/bash

mvn --settings /root/tools/settings.xml -Pnative clean package

OCCLUM_RELEASE_ENCLAVE=true java -Dcom.alibaba.enclave.metric.enable=false -cp host/target/host-1.0-SNAPSHOT-jar-with-dependencies.jar:enclave/target/enclave-1.0-SNAPSHOT-jar-with-dependencies.jar com.alibaba.confidentialcomputing.benchmark.guomi.host.GuoMiBenchMark
