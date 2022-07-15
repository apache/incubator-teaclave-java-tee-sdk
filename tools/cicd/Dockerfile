FROM ubuntu:18.04

LABEL maintainer="Junshao Wang <jeffery.wsj@alibaba-inc.com>"

ENV APT_KEY_DONT_WARN_ON_DANGEROUS_USAGE=1
ENV DEBIAN_FRONTEND noninteractive

ADD ["graalvm-enclave-22.1.0.tar", "/root/tools/"]
ADD ["x86_64-linux-musl-native.tgz", "/root/tools/"]
ADD ["zlib-1.2.11.tar.gz", "/root/tools/"]
ADD ["settings.xml", "/root/tools/"]
ADD ["sgx_linux_x64_sdk_2.17.100.0.bin", "/root/tools/"]
ENV GRAALVM_HOME "/root/tools/graalvm-enclave-22.1.0"
ENV JAVA_HOME "/root/tools/graalvm-enclave-22.1.0"
ENV CC "/root/tools/x86_64-linux-musl-native/bin/gcc"
ENV PATH $PATH:"/root/tools/x86_64-linux-musl-native/bin"

ARG PSW_VERSION=2.17.100.3
ARG DCAP_VERSION=1.14.100.3

# install necessary tools.
RUN apt-get update && apt-get install -y gdb gnupg wget aptitude && \
    echo 'deb [arch=amd64] https://download.01.org/intel-sgx/sgx_repo/ubuntu bionic main' > /etc/apt/sources.list.d/intel-sgx.list && \
    wget -qO - https://download.01.org/intel-sgx/sgx_repo/ubuntu/intel-sgx-deb.key | apt-key add - && \
    apt-get update && aptitude install -y \
    libsgx-launch-dev=$PSW_VERSION-bionic1 \
    libsgx-urts=$PSW_VERSION-bionic1 \
    libsgx-urts-dbgsym=$PSW_VERSION-bionic1 \
    libsgx-uae-service=$PSW_VERSION-bionic1 \
    libsgx-dcap-quote-verify-dev=$DCAP_VERSION-bionic1 \
    libsgx-dcap-ql-dev=$DCAP_VERSION-bionic1 \
    libsgx-dcap-default-qpl=$DCAP_VERSION-bionic1 && \
    echo -e 'yes\n' | apt-get install -y maven && \
    echo -e 'yes\n' | apt-get install -y build-essential libz-dev zlib1g-dev && \
    cd /root/tools/zlib-1.2.11 && ./configure --prefix=/root/tools/x86_64-linux-musl-native --static && make && make install && \
    cd /root/tools && chmod 777 sgx_linux_x64_sdk_2.17.100.0.bin && echo -e 'no\n/opt/teesdk\n' | ./sgx_linux_x64_sdk_2.17.100.0.bin
