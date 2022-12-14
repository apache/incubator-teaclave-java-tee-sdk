GraalVM can statically compile Java application into native library to run inside TEE's enclave environment. However, there is still no standards for Enclave SDKs. Different SDK providers ( e.g. Intel TEE SDK, Microsoft Open Enclave SDK, etc.) may have different implementations for the same system behaviors, and different from the standard C implementations as well. On the other hand, as a general purpose static compilatation framework, GraalVM depends on the standard (Linux, Windows or Mac, but we only focus on Linux here) system calls and structures. Therefore, there are some incompatibilities between GraalVM and Enclave SDKs. This doc explains how these incompatibilities are addressed in Java Enclave project.
# JVM level
## The obtaining of pseudo-random number
In OpenJDK, the pseudo-random number is obtained in`sun.security.provider.NativePRNG`class by accessing two special IO devices, `/dev/random` and `/dev/urandom`. But these virtual IO devices are inaccessbile from Enclave, leading to `IOException` when calling `NativePRNG` class. 
The solution is to stop accessing these two IO devices, but invoke a system level random function. Classes `org.apache.teaclave.javasdk.enclave.substitutes.NativePRNGSubstitutions` and `org.apache.teaclave.javasdk.enclave.EnclaveRandomFeature` take care of this issue.

## libsunec.a has C++ symbols
Libsunec.a has C++ symbols, `new` and `delete`, which are not supported by musl, in its JNI native implementations. More specifically, there are 5 native methods has C++ symbols:

+ `sun.security.ec.ECKeyPairGenerator#generateECKeyPair`
+ `sun.security.ec.ECKeyPairGenerator#isCurveSupported`
+ `sun.security.ec.ECDSASignature#signDigest`
+ `sun.security.ec.ECDSASignature#verifySignedDigest`
+ `sun.security.ec.ECDHKeyAgreement#deriveKey`

We rewrite the native JNI code of above 5 methods by replacing `new` with `malloc`, and `delete` with `free`. Then the pure C symbols sunec library is compiled and saved as libenc_sunec.a. The native calls to these 5 methods are redirected to libenc_sunec.a while other native calls still go to the original libsunec.a.

See `src/main/resources/native/sunec/org_apache_teaclave_javasdk_enclave_substitutes_NativeSunECMethods.h`, `org.apache.teaclave.javasdk.enclave.SUNECReplaceFeature`, `org.apache.teaclave.javasdk.enclave.substitutes.NativeSunECMethods` and `org.apache.teaclave.javasdk.enclave.substitutes.SUNECSubstitutions` for more details.
# System level
## CPU features check
Since 22.1.0, GraalVM reads CPU features and checks which is supported at runtime, so that it doesn't only rely on the statically set CPU features (see `com.oracle.svm.core.cpufeature.RuntimeCPUFeatureCheckFeature`).
But Enclave SDKs don't support reading CPU features at runtime.
A solution is to disable the `RuntimeCPUFeatureCheckFeature` (see `org.apache.teaclave.javasdk.enclave.EnclaveFeature#afterRegistration`).

Since 22.2.0, GraalVM verifies the cpu features as early as program starts (see `com.oracle.svm.core.JavaMainWrapper#run`), but the CPU checking functions are not supported by Enclave SDKs.
The checking is performed by the `CPUFeatureAccess` instance which is set `ImageSingletons` by `com.oracle.svm.hosted.AMD64CPUFeatureAccessFeature` at image build time.
So we added a subclass of `AMD64CPUFeatureAccessFeature`, `org.apache.teaclave.javasdk.enclave.cpufeatures.EnclaveAMD64CPUFeatureAccessFeature` to override the behavior of setting `CPUFeatureAccess` instance.
`EnclaveAMD64CPUFeatureAccessFeature` sets `org.apache.teaclave.javasdk.enclave.cpufeatures.EnclaveAMD64CPUFeatureAccess` instance into `ImageSingletons` instead of the original `AMD64CPUFeatureAccess`.

## Memory 
Enclave SDKs don't support reading system memory information from standard POSIX interfaces:

1. `sysconf(_SC_PHYS_PAGES())` returns -1.
1. `sysconf(_SC_PAGESSIZE())` returns -1.
1. `sysconf(_SC_PAGE_SISE())` returns -1.

GraalVM's native image gets the physical memory from the first 2 functions, and gets the virual memory from the 3rd function.
The solution is to replace the unsupported reading with other native functions. See `org.apache.teaclave.javasdkg.enclave.system.EnclaveMemoryFeature`, `org.apache.teaclave.javasdk.enclave.system.EnclaveVirtualMemoryProvider` and `org.apache.teaclave.javasdk.enclave.system.EnclavePhysicalMemory` for implementation details.
