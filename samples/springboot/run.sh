#!/bin/bash

# step1: build springboot application service.
mvn --settings /root/tools/settings.xml -Pnative clean package

# step2: startup springboot application service.
java -cp host/target/host-1.0-SNAPSHOT-jar-with-dependencies.jar:enclave/target/enclave-1.0-SNAPSHOT-jar-with-dependencies.jar com.alibaba.confidentialcomputing.samples.springboot.host.Application &
sleep 5
# shellcheck disable=SC2006
# shellcheck disable=SC2009
PID=`ps -ef | grep "Application" | grep -v grep | awk '{print $2}'`

# step3: encrypt and decrypt service.
wget http://localhost:8080/enclaveEncDecService?data=SELECT+TOP+50+PERCENT+*+FROM+Websites;

# step4: digest service.
wget http://localhost:8080/enclaveDigestService?data=SELECT+TOP+50+PERCENT+*+FROM+Websites;

# step5: kill springboot service.
kill -9 "$PID"