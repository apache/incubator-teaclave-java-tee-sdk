# JavaEnclave Configuration Illustration

## Configure file

JavaEnclave provides a `java_enclave_configure.json` template file for user to set customized parameters. It provides six parameters:

| key                            | value(default)  | illustation                                                                                                                                                                                                              |
|--------------------------------| ------------ |--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| debuggable                     | false  | Allow `TEE_SDK` Enclave or `EMBEDDED_LIB_OS` Enclave to be debuggable or not, debuggable mode help to debug. Should make it to be not debuggable when the project is online service for security.                        |
| enclave_type                   | TEE_SDK  | When creating an enclave instance with method `EnclaveFactory.create()`, TEE_SDK kind of Enclave will be created by default, key `enclave_type` could be one of `MOCK_IN_JVM` `MOCK_IN_SVM` `TEE_SDK` `EMBEDDED_LIB_OS`. |
| metric_trace_enable            | false  | Enable JavaEnclave performance metric or not.                                                                                                                                                                            |
| metric_trace_file_path         |  "" | Customized JavaEnclave metric log file path. JavaEnclave Metric feature could help to measure the cost of every service invocation and service loading/unloading.                                                        |
| enclave_max_thread             | 50  | The max thread number ecalls into `TEE_SDK` Enclave or `EMBEDDED_LIB_OS` Enclave.                                                                                                                                        |
| enclave_max_epc_memory_size_MB |  1500  | The max physical epc memory size in `TEE_SDK`, `EMBEDDED_LIB_OS` Enclave                                                                                                                                                  |

`debuggable`、`enclave_type`、`metric_trace_enable` and  `metric_trace_file_path`could also be set by -Dproperty way. But`enclave_max_thread ` and `enclave_max_epc_memory_size_MB `could only be changed or set by java_enclave_configure.json file in enclave submodule's resource dir.

## Property Configuration Setting

JavaEnclave provides some customized property setting for different scene.

| property  | value  | illustration  |
| ------------ | ------------ | ------------ |
| com.alibaba.enclave.type  | MOCK_IN_JVMMOCK_IN_SVM/TEE_SDK/EMBEDDED_LIB_OS  |  same as be described in Configure file   |
| com.alibaba.enclave.debuggable  | true/false  | same as be described in Configure file   |
| com.alibaba.enclave.metric.enable  | true/false  | same as be described in Configure file   |
| com.alibaba.enclave.metric.log  | customized metric log file path  | same as be described in Configure file   |

### MOCK_IN_SVM Enclave Property Configuration Setting

| property                                 | value       | illustration                                 |
|------------------------------------------|-------------|----------------------------------------------|
| com.alibaba.enclave.mockinsvm.maxheap_MB |             | gc max heap size(MB) in mock_in_svm enclave. |

### TEE_SDK Enclave Property Configuration Setting

| property                                      | value       | illustration                                                                      |
|-----------------------------------------------|-------------|-----------------------------------------------------------------------------------|
| com.alibaba.enclave.teesdk.symbol.trace | true(false) | help to trace undefined symbols invocation in TEE_SDK, default value is unenable. |
| com.alibaba.enclave.teesdk.maxheap_MB         |             | gc max heap size(MB) in tee sdk enclave.                                          |


### EMBEDDED_LIB_OS Enclave Property Configuration Setting

| property                                        |  value |  illustration |
|-------------------------------------------------| ------------ | ------------ |
| com.alibaba.enclave.agent.thread.pool.size      | 5  |  agent http server thread pool size in enclave, default value is 5.  |
| com.alibaba.enclave.embedded.startup.timeout_ms | 60000  | the max startup timeout for libos enclave startup, default timeout is 60000ms.  |
| com.alibaba.enclave.embedded.keepalive.timeout_s | 300  |  enclave agent http long connection keep alive timeout, default timeout is 300s.  |
| com.alibaba.enclave.embedded.keepalive.max      | 100  | max agent http router, default number is 100.  |
| com.alibaba.enclave.embedded.connect.timeout_ms | 1000  |  agent http connection timeout, default is 1000ms. |
| com.alibaba.enclave.embedded.read.timeout_ms    | 2000  | agent http reading timeout, default is 2000ms.  |
| com.alibaba.enclave.embedded.ra.timeout_ms      | 10000  |  embedded libos remote attestation generation and verification timeout, default is 10000ms.  |
| com.alibaba.enclave.embedded.log.level          | "off" | enable enclave log or not, default is off. |
| com.alibaba.enclave.embedded.jvm.args           | "-Dsun.net.httpserver.nodelay=true,  -XX:-UseCompressedOops,  -Xmx800m,  -Dos.name=Linux" | jvm's startup args in embedded libos enclave. |
