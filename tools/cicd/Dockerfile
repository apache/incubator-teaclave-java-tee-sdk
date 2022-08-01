FROM ubuntu:18.04

LABEL maintainer="Junshao Wang <jeffery.wsj@alibaba-inc.com>"

ENV APT_KEY_DONT_WARN_ON_DANGEROUS_USAGE=1
ENV DEBIAN_FRONTEND noninteractive

ADD ["graalvm-ce-java11-22.2.0.tar", "/root/tools/"]
ADD ["zlib-1.2.11.tar.gz", "/root/tools/"]
ADD ["settings.xml", "/root/tools/"]
ADD ["zlib-1.2.11.tar.gz", "/root/tools/"]
ADD ["Alibaba_Dragonwell_11.0.15.11.9_x64_alpine-linux.tar.gz", "/root/tools"]
ADD ["sgx_linux_x64_sdk_2.17.100.0.bin", "/root/tools/"]
ENV GRAALVM_HOME "/root/tools/graalvm-ce-java11-22.2.0"
ENV JAVA_HOME "/root/tools/graalvm-ce-java11-22.2.0"
ENV PATH="/opt/occlum/build/bin:/usr/local/occlum/bin:$PATH"
ENV CC=/usr/local/occlum/bin/occlum-gcc

ARG PSW_VERSION=2.17.100.3
ARG DCAP_VERSION=1.14.100.3

# install necessary tools.
RUN apt-get update && apt-get install -y gdb gnupg wget aptitude libfuse-dev libtool tzdata jq && \
    echo -e 'yes\n' | apt-get install -y maven && \
    echo -e 'yes\n' | apt-get install -y build-essential libz-dev zlib1g-dev && \
    echo 'deb [arch=amd64] https://download.01.org/intel-sgx/sgx_repo/ubuntu bionic main' > /etc/apt/sources.list.d/intel-sgx.list && \
    wget -qO - https://download.01.org/intel-sgx/sgx_repo/ubuntu/intel-sgx-deb.key | apt-key add - && \
    echo 'deb [arch=amd64] https://occlum.io/occlum-package-repos/debian bionic main' | tee /etc/apt/sources.list.d/occlum.list && \
    wget -qO - https://occlum.io/occlum-package-repos/debian/public.key | apt-key add - && \
    apt-get update && aptitude install -y \
    occlum \
    libsgx-launch-dev=$PSW_VERSION-bionic1 \
    libsgx-urts=$PSW_VERSION-bionic1 \
    libsgx-urts-dbgsym=$PSW_VERSION-bionic1 \
    libsgx-uae-service=$PSW_VERSION-bionic1 \
    libsgx-dcap-quote-verify-dev=$DCAP_VERSION-bionic1 \
    libsgx-dcap-ql-dev=$DCAP_VERSION-bionic1 \
    libsgx-dcap-default-qpl=$DCAP_VERSION-bionic1 && \
    echo -e 'yes\n' | apt-get install -y maven && \
    echo -e 'yes\n' | apt-get install -y build-essential libz-dev zlib1g-dev && \
    cd /root/tools/zlib-1.2.11 && ./configure --prefix=/opt/occlum/toolchains/gcc/x86_64-linux-musl && make && make install && \
    cd /root/tools && chmod 777 sgx_linux_x64_sdk_2.17.100.0.bin && echo -e 'no\n/opt/teesdk\n' | ./sgx_linux_x64_sdk_2.17.100.0.bin

# copy dcap_occlum lib from occlum docker image.
COPY --from=occlum/occlum:0.26.4-ubuntu18.04 /opt/occlum/toolchains/dcap_lib /opt/occlum/toolchains/dcap_lib
