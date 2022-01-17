FROM ubuntu:18.04 AS base

LABEL maintainer="Junshao Wang <jeffery.wsj@alibaba-inc.com>"

ENV APT_KEY_DONT_WARN_ON_DANGEROUS_USAGE=1
ENV DEBIAN_FRONTEND noninteractive

# install openjdk11 and maven.
RUN apt-get update && \
    echo -e 'yes\n' | apt-get install -y openjdk-11-jdk && \
    echo -e 'yes\n' | apt-get install -y maven
