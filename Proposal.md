======== Proposal =======

## Abstract

Teaclave Java TEE SDK is a Java confidential computing programming framework. It follows the host-and-enclave partition programming model defined by Intel-SGX SDK. Teaclave Java TEE SDK provides an elegant way to divide a java project into host and enclave modules, where the enclave module is a provider of a user-defined service interface which is similar to the Java SPI model. Teaclave Java TEE SDK could help you to develop and build a Java confidential computing project with high efficiency.

## Proposal

Teaclave Java TEE SDK is a pure Java SDK for Java confidential computing. It eases the interactions between secured and unsecured environment with a few concise APIs. From user's aspect, creating an enclave environment and invoking confidential computing services would be as simple as invoking SPI services.

### Background

The Teaclave Java TEE SDK project is being actively developed within Alibaba Cloud.

### Rationale

Teaclave Java TEE SDK is a Java confidential computing programming framework. The goal of Teaclave is to provide a universal secure computing platform for multiple programming languages. Teaclave currently supports Rust, Python and WebAssembly, but Java is still missing. Teaclave Java TEE SDK would be an important piece of the puzzle.

### Initial Goals

- Transfer the repository to the Apache Incubator under the Teaclave project
- Code cleanup and more documentation

#### Meritocracy:

Teaclave Java TEE SDK project was originally developed and reviewed by Shaojun Wang/Ziyi Lin/Lei Yu/Sanhong Li within Alibaba Cloud. We encourage everyone to ask questions and create pull requests for the project.

#### Community:

Teaclave Java TEE SDK was developed and applied within Alibaba Cloud before it was donated to Teaclave.

#### Core Developers:

The core developers are:
- Shaojun Wang (jeffery.wsj@alibaba-inc.com)
- Ziyi Lin (cengfeng.lzy@alibaba-inc.com)

#### Alignment:

The project is complimentary of Teaclave's TEE backends.

### Known Risks

An exercise in self-knowledge. Risks don't mean that a project is unacceptable. If they are recognized and noted, then they can be addressed during incubation.

#### Project Name

Teaclave Java TEE SDK was initially developed and applied within Alibaba Cloud as a closed-source project which was called JavaEnclave.

#### Inexperience with Open Source:

Teaclave Java TEE SDK has been reviewed by Mingshen Sun, who is from the Teaclave community. He's familiar with The Apache Way for the open-source community.

#### Length of Incubation:

The project will be in incubation with Apache Teaclave (incubating) project.

### Documentation

- Teaclave Java TEE SDK's documentation will be submit to Teaclave PPMC for review by email private@teaclave.apache.org

### Initial Source

- Teaclave Java TEE SDK's source code will be submit to Teaclave PPMC for review by email private@teaclave.apache.org

### Source and Intellectual Property Submission Plan

We will submit a Software Grant for this project later.

#### External Dependencies:

The dependencies have Apache compatible license, which is provided under the BSD 2-Clause license and GPL2.0 license. One dependency of Teaclave Java TEE SDK is GraalVM SubstraceVM with GPL2.0 license, it's used as the enclave module's native image compiler, and Teaclave Java TEE SDK has contributed some patches to GraalVM and they had been contained in GraalVM's official releases assets. Teaclave Java TEE SDK is dependent on GraalVM's official releases and doesn't make any modifications to GraalVM's source code, so there's no legal risk.
https://www.apache.org/legal/resolved.html

#### Cryptography:

N/A

### Required Resources

#### Mailing lists:

The project shares the same mailing list of Teaclave.

#### Git Repositories:

- N/A

#### Issue Tracking:

Same with Teaclave.

#### Other Resources:

N/A

### Initial Committers

- Shaojun Wang (jeffery.wsj@alibaba-inc.com)
- Ziyi Lin (cengfeng.lzy@alibaba-inc.com)
- Lei Yu (lei.yul@alibaba-inc.com)
- Sanhong Li (sanhong.lsh@alibaba-inc.com)