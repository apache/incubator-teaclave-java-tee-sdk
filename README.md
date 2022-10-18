## What's Teaclave Java TEE SDK?

Teaclave Java TEE SDK is a Java confidential computing programming framework. It follows the host-and-enclave partition programming model defined by Intel-SGX SDK. Teaclave Java TEE SDK provides an elegant way to divide a java project into host and enclave modules, where the enclave module is a provider of a user-defined service interface which is similar to the Java SPI model. Teaclave Java TEE SDK could help you to develop and build a Java confidential computing project with high efficiency.

## Why do we need Teaclave Java TEE SDK?

Occlum and Gramine libOS solutions run the entire Java application inside the enclave. Although it's much easier for end users, it suffers from a large TCB(Trusted Computing Base) that may compromise the security to some degree. On the other hand, Intel-SGX and OpenEnclave SDKs are more secure by only running the sensitive code inside the enclave, but they are limited to C/C++ ecosystem, and the development experience for programmers is unfriendly. For Example, it requests the programmer to provide an unknown .edl file, which defines the interface between host and enclave. While Teaclave Java TEE SDK provides a Pure Java SDK API for Java confidential computing application development. It eases the interactions between secured and unsecured environment with a few concise APIs. From user's aspect, creating an enclave environment and invoking confidential computing services would be as simple as invoking SPI services.

## Teaclave Java TEE SDK architecture

Teaclave Java TEE SDK provides seven components:

- Teaclave Java TEE SDK Host.jar, provides API to create and destroy enclave instances, enclave service loading and unloading, remote attestation quote generation, and verification.

- Teaclave Java TEE SDK Enclave.jar, makes java native image runs in sgx enclave environment, and provides a stub between host and enclave for their interaction.

- Teaclave Java TEE SDK Common.jar, provides an annotation for application, which helps to register user-defined interface parameters' type information for native image reflection. Also, it defines the interface between host and enclave for underlying interaction, and it's transparent for the application.

- Teaclave Java TEE SDK, provides all kinds of underlying JNI .so and building toolchains.

- Teaclave Java TEE SDK Archetype project, helps the user to create a Java confidential computing project Structure.

- Native BouncyCastle third-party package, helps the user to apply BouncyCastle in the enclave native environment without reflection issues.

- Teaclave Java TEE SDK Docker, provides a standard build and execution environment for Java confidential computing applications.

<br />
<div  align="center">
<img src="./docs/resources/JavaEnclave_Architecture.png" width = "400" height = "400" alt="Teaclave Java TEE SDK Architecture" align=center />
</div>
<center>Teaclave Java TEE SDK Architecture</center>
<br />

## Confidential computing Java project structure based on Teaclave Java TEE SDK

A Java confidential computing application project based on Teaclave Java TEE SDK is a maven project which consists of three submodules, they are host submodule, enclave submodule, and common submodule. The common submodule contains the service interface definition, the enclave submodule implements the interface defined in the common submodule, host submodule contains the management of the enclave instance and service instance. We can view the enclave submodule as an SPI provider, Teaclave Java TEE SDK will help to run the provider in an enclave, the provider could be compiled to a native image or a jar file.

<br />
<div  align="center">
<img src="./docs/resources/JavaEnclave_Application_Dependency.png" width = "400" height = "300" alt="Teaclave Java TEE SDK Application Dependency" align=center />
</div>
<center>Teaclave Java TEE SDK Application Dependency</center>
<br />
<div  align="center">
<img src="./docs/resources/JavaEnclave_Project_Structure.png" width = "400" height = "400" alt="Teaclave Java TEE SDK Project Structure" align=center />
</div>
<center>Teaclave Java TEE SDK Project Structure</center>
<br />

## Getting started

### Environment preparation

#### 1. Is SGX2 supported?

`apt install cpuid && cpuid -1 -l 0x12`

<img src="./docs/resources/SGX2_Supported_Check.png" width = "300" height = "100" alt="Teaclave Java TEE SDK Application Dependency" align=center />

if SGX2 is not supported, only MOCK_IN_JVM and MOCK_IN_SVM enclave modes in Teaclave Java TEE SDK could be run normally.

#### 2. Is the SGX2 driver installed?

`cd /dev` and check whether `sgx_enclave sgx_provision` soft link files exist.

if it is not, you need to install the sgx driver according to reference: https://github.com/intel/linux-sgx-driver.

#### 3. enable_rdfsbase kernel module

if Linux kernel before 5.9, please install the enable_rdfsbase kernel module according to reference: https://github.com/occlum/enable_rdfsbase. enable_rdfsbase kernel module is needed if you create an enclave instance with EMBEDDED_LIB_OS mode defined in Teaclave Java TEE SDK.

#### 4. Enter Teaclave Java TEE SDK docker

`docker run -it --privileged --network host -v /dev/sgx_enclave:/dev/sgx/enclave -v /dev/sgx_provision:/dev/sgx/provision teaclave-java-tee-sdk:v0.1.0-ubuntu18.04`

Teaclave Java TEE SDK Docker provides a compilation and deployment environment for a java confidential computing application based on Teaclave Java TEE SDK.

### HelloWorld sample instruction

#### 1. Create a HelloWorld project structure

Teaclave Java TEE SDK provides a java confidential computing archetype project to help us create a basic project structure.

`mvn archetype:generate -DgroupId=com.sample -DartifactId=helloworld -DarchetypeGroupId=org.apache.teaclave.javasdk -DarchetypeArtifactId=javaenclave-archetype -DarchetypeVersion=0.1.0 -DinteractiveMode=false`

archetype creates a maven project with three submodules, a host submodule enclave submodule, and a common submodule.

#### 2. Define enclave service interface in the common submodule

`cd helloworld/common/src/main/java/com/sample/` and create a common package in this submodule `mkdir -p helloworld/common`.

then create a Service.java file to define an enclave service interface.

```java
package com.sample.helloworld.common;

import org.apache.teaclave.javasdk.common.annotations.EnclaveService;

@EnclaveService
public interface Service {
    String sayHelloWorld();
}
```

Note that we have to annotate this service interface with `@EnclaveService` which Teaclave Java TEE SDK provides.

#### 3. Create enclave service interface provider in enclave submodule

`cd helloworld/enclave/src/main/java/com/sample/` and create an enclave package in this submodule `mkdir -p helloworld/enclave`.

then create ServiceImpl.java to implement the service interface defined in the common package.

```java
package com.sample.helloworld.enclave;

import com.sample.helloworld.common.Service;
import com.google.auto.service.AutoService;

@AutoService(Service.class)
public class ServiceImpl implements Service {
    @Override
    public String sayHelloWorld() {
        return "Hello World";
    }
}
```

Note that we have to annotate this class with the annotation `@AutoService(Interface. class)`.

#### 4. Develop host submodule to create and invoke enclave service

`cd helloworld/host/src/main/java/com/sample/` and create an host package in this submodule `mkdir -p helloworld/host`.

then create Main.java to show how to create and invoke an enclave service.

```java
package com.sample.helloworld.host;

import org.apache.teaclave.javasdk.host.Enclave;
import org.apache.teaclave.javasdk.host.EnclaveFactory;
import org.apache.teaclave.javasdk.host.EnclaveType;

import com.sample.helloworld.common.Service;

import java.util.Iterator;

public class Main {
    public static void main(String[] args) throws Exception {
        EnclaveType[] enclaveTypes = {
                EnclaveType.MOCK_IN_JVM,
                EnclaveType.MOCK_IN_SVM,
                EnclaveType.TEE_SDK,
                EnclaveType.EMBEDDED_LIB_OS};

        for (EnclaveType enclaveType : enclaveTypes) {
            Enclave enclave = EnclaveFactory.create(enclaveType);
            Iterator<Service> services = enclave.load(Service.class);
            System.out.println(services.next().sayHelloWorld());
            enclave.destroy();
        }
    }
}
```

#### 5. Build and run

cd back to HelloWorld project top dir and build it: `mvn -Pnative clean package`.

Note that parameter `-Pnative` should not be ignored.

then we could run this sample: `OCCLUM_RELEASE_ENCLAVE=true java -cp host/target/host-1.0-SNAPSHOT-jar-with-dependencies.jar:enclave/target/enclave-1.0-SNAPSHOT-jar-with-dependencies.jar com.sample.helloworld.host.Main`

## Four enclave types in Teaclave Java TEE SDK

### MOCK_IN_JVM mode

`MOCK_IN_JVM` mode in Teaclave Java TEE SDK is a simulated mode, it doesn't need SGX hardware support. The host module and enclave module run in the same JVM environment.
In essence, it's an SPI mechanism between host and enclave parts.

### MOCK_IN_SVM mode

`MOCK_IN_SVM` mode in Teaclave Java TEE SDK is also a simulated mode, it doesn't need SGX hardware support. Compare with `MOCK_IN_JVM` mode, the enclave submodule
will be compiled into a native image, and the host submodule run in a JVM environment. host part will load, create and invoke service defined in enclave by JNI native call.

### TEE_SDK mode

`TEE_SDK` mode is a hardware mode, it must run on the platform with SGX2 hardware support. Compare with `MOCK_IN_SVM` mode, the enclave submodule also will be compiled into a native image, but it will be loaded and run in sgx enclave environment. The host part will run in a JVM environment, and both the host and enclave module will run in one process.

### EMBEDDED_LIB_OS mode

`EMBEDDED_LIB_OS` mode is also a hardware mode, it must run on the platform with SGX2 hardware support. Compare with `TEE_SDK` mode, the enclave submodule will be compiled into a jar file, and it will be loaded and run in an enclave with libOS Occlum, an inner alpine JVM runs based on this libOS. The host part runs in another JVM based on a normal environment. The two JVM instances co-existence and run in one process.

## Teaclave Java TEE SDK configuration

please refer to the link: [Configuration.md](./sdk/host/docs/Configuration.md)