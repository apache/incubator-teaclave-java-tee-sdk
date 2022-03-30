FROM ubuntu:18.04

LABEL maintainer="Junshao Wang <jeffery.wsj@alibaba-inc.com>"

ENV APT_KEY_DONT_WARN_ON_DANGEROUS_USAGE=1
ENV DEBIAN_FRONTEND noninteractive

ADD ["graalvm-enclave-22.0.0.tar", "/root/tools/"]
ADD ["x86_64-linux-musl-native.tgz", "/root/tools/"]
ADD ["zlib-1.2.12.tar.gz", "/root/tools/"]
ADD ["settings.xml", "/root/tools/"]
ENV GRAALVM_HOME "/root/tools/graalvm-enclave-22.0.0"
ENV JAVA_HOME "/root/tools/graalvm-enclave-22.0.0"
ENV CC "/root/tools/x86_64-linux-musl-native/bin/gcc"
ENV PATH $PATH:"/root/tools/x86_64-linux-musl-native/bin"

# install necessary tools.
RUN apt-get update && \
    echo -e 'yes\n' | apt-get install -y maven && \
    echo -e 'yes\n' | apt-get install -y build-essential libz-dev zlib1g-dev && \
    cd /root/tools/zlib-1.2.12 && ./configure --prefix=/root/tools/x86_64-linux-musl-native --static && make && make install
