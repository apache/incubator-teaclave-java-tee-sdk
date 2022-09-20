# Teaclave-java-tee-sdk Configuration Illustration

## Configure file

Teaclave-java-tee-sdk provides a `java_enclave_configure.json` template file for user to set customized parameters. It provides six parameters:

| key                            | value(default) | illustration                                                                                                                                                                                                             |
|--------------------------------|----------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| debuggable                     | false          | Allow `TEE_SDK` Enclave or `EMBEDDED_LIB_OS` Enclave to be debuggable or not, debuggable mode help to debug. Should make it to be not debuggable when the project is online service for security.                        |
| enclave_type                   | TEE_SDK        | When creating an enclave instance with method `EnclaveFactory.create()`, TEE_SDK kind of Enclave will be created by default, key `enclave_type` could be one of `MOCK_IN_JVM` `MOCK_IN_SVM` `TEE_SDK` `EMBEDDED_LIB_OS`. |
| metric_trace_enable            | false          | Enable Teaclave-java-tee-sdk performance metric or not.                                                                                                                                                                  |
| metric_trace_file_path         | ""             | Customized Teaclave-java-tee-sdk metric log file path. Teaclave-java-tee-sdk Metric feature could help to measure the cost of every service invocation and service loading/unloading.                                    |
| enclave_max_thread             | 50             | The max thread number which enclave allows to be e-called into `TEE_SDK` Enclave or `EMBEDDED_LIB_OS` Enclave.                                                                                                           |
| enclave_max_epc_memory_size_MB | 1500           | The max physical epc memory size in `TEE_SDK`, `EMBEDDED_LIB_OS` Enclave                                                                                                                                                 |

`debuggable`、`enclave_type`、`metric_trace_enable` and  `metric_trace_file_path`could also be set by -Dproperty way. But`enclave_max_thread ` and `enclave_max_epc_memory_size_MB `could only be changed or set by java_enclave_configure.json file in enclave submodule's resource dir.

## Property Configuration Setting

Teaclave-java-tee-sdk provides some customized property setting for different scene.

| property                                          | value                                           | illustration                           |
|---------------------------------------------------|-------------------------------------------------|----------------------------------------|
| org.apache.teaclave.javasdk.enclave.type          | MOCK_IN_JVM/MOCK_IN_SVM/TEE_SDK/EMBEDDED_LIB_OS | same as be described in Configure file |
| org.apache.teaclave.javasdk.enclave.debuggable    | true/false                                      | same as be described in Configure file |
| org.apache.teaclave.javasdk.enclave.metric.enable | true/false                                      | same as be described in Configure file |
| org.apache.teaclave.javasdk.enclave.metric.log    | customized metric log file path                 | same as be described in Configure file |

### MOCK_IN_SVM Enclave Property Configuration Setting

| property                                                 | value | illustration                                 |
|----------------------------------------------------------|-------|----------------------------------------------|
| org.apache.teaclave.javasdk.enclave.mockinsvm.maxheap_MB |       | gc max heap size(MB) in mock_in_svm enclave. |

### TEE_SDK Enclave Property Configuration Setting

| property                                                | value       | illustration                                                                     |
|---------------------------------------------------------|-------------|----------------------------------------------------------------------------------|
| org.apache.teaclave.javasdk.enclave.teesdk.symbol.trace | true(false) | help to trace undefined symbols invocation in TEE_SDK, default value is disable. |
| org.apache.teaclave.javasdk.enclave.teesdk.maxheap_MB   |             | gc max heap size(MB) in tee sdk enclave.                                         |


### EMBEDDED_LIB_OS Enclave Property Configuration Setting

| property                                                         | value                                                                                  | illustration                                                                                |
|------------------------------------------------------------------|----------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------|
| org.apache.teaclave.javasdk.enclave.agent.thread.pool.size       | 5                                                                                      | agent http server thread pool size in enclave, default value is 5.                          |
| org.apache.teaclave.javasdk.enclave.embedded.startup.timeout_ms  | 60000                                                                                  | the max startup timeout for lib os enclave startup, default timeout is 60000ms.             |
| org.apache.teaclave.javasdk.enclave.embedded.keepalive.timeout_s | 300                                                                                    | enclave agent http long connection keep alive timeout, default timeout is 300s.             |
| org.apache.teaclave.javasdk.enclave.embedded.keepalive.max       | 100                                                                                    | max agent http router, default number is 100.                                               |
| org.apache.teaclave.javasdk.enclave.embedded.connect.timeout_ms  | 1000                                                                                   | agent http connection timeout, default is 1000ms.                                           |
| org.apache.teaclave.javasdk.enclave.embedded.read.timeout_ms     | 2000                                                                                   | agent http reading timeout, default is 2000ms.                                              |
| org.apache.teaclave.javasdk.enclave.embedded.ra.timeout_ms       | 10000                                                                                  | embedded lib os remote attestation generation and verification timeout, default is 10000ms. |
| org.apache.teaclave.javasdk.enclave.embedded.log.level           | "off"                                                                                  | enable enclave log or not, default is off.                                                  |
| org.apache.teaclave.javasdk.enclave.embedded.jvm.args            | "-Dsun.net.httpserver.nodelay=true, -XX:-UseCompressedOops, -Xmx800m, -Dos.name=Linux" | jvm's startup args in embedded lib os enclave.                                              |
