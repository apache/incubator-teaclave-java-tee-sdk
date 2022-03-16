FROM ubuntu:18.04 AS base

LABEL maintainer="Junshao Wang <jeffery.wsj@alibaba-inc.com>"

ENV APT_KEY_DONT_WARN_ON_DANGEROUS_USAGE=1
ENV DEBIAN_FRONTEND noninteractive

# install openjdk11 and maven.
RUN apt-get update && \
    echo -e 'yes\n' | apt-get install -y maven && \
    echo -e 'yes\n' | apt-get install -y build-essential libz-dev zlib1g-dev && \
    echo -e 'yes\n' | apt-get install -y wget
ADD ["graalvm-enclave-22.0.0.tar", "/root/tools/"]
ENV GRAALVM_HOME "/root/tools/graalvm-enclave-22.0.0"
ENV JAVA_HOME "/root/tools/graalvm-enclave-22.0.0"
