// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

#include <jni.h>

#include "generate_attestation_report.h"

#ifndef _Included_jni_tee_sdk_svm
#define _Included_jni_tee_sdk_svm

typedef void (*enclave_calling_stub)(jlong, int*, graal_isolate_t*, void*, size_t, void*, size_t*);

typedef struct {
    int        ret;
    jbyteArray result;
} enclave_calling_stub_result;

#define REMOTE_ATTESTATION_CLASS_NAME                "org/apache/teaclave/javasdk/host/exception/RemoteAttestationException"
#define ENCLAVE_CREATING_EXCEPTION                   "org/apache/teaclave/javasdk/host/exception/EnclaveCreatingException"
#define ENCLAVE_DESTROYING_EXCEPTION                 "org/apache/teaclave/javasdk/host/exception/EnclaveDestroyingException"
#define ENCLAVE_SERVICE_LOADING_EXCEPTION            "org/apache/teaclave/javasdk/host/exception/ServicesLoadingException"
#define ENCLAVE_SERVICE_UNLOADING_EXCEPTION          "org/apache/teaclave/javasdk/host/exception/ServicesUnloadingException"
#define ENCLAVE_SERVICE_INVOKING_EXCEPTION           "org/apache/teaclave/javasdk/host/exception/EnclaveMethodInvokingException"

#define TEE_SDK_SVM_NATIVE_CALL_SIGNATURE            "(JJ[B)[B"
#define TEE_SDK_REMOTE_ATTESTATION_REPORT_SIGNATURE  "(J[B)Lorg/apache/teaclave/javasdk/host/TeeSdkAttestationReport;"
#define TEE_SDK_REMOTE_ATTESTATION_REPORT_CLASS_NAME "org/apache/teaclave/javasdk/host/TeeSdkAttestationReport"

#define THROW_EXCEPTION(env, exception, info)                                  \
{                                                                              \
    jclass ra_class = (*env)->FindClass(env, exception);                       \
    if (ra_class == NULL) {                                                    \
        fprintf(stderr, "Teaclave Java TEE SDK Error:  ");                     \
        fprintf(stderr, exception);                                            \
        fprintf(stderr, " class loading failed.\n");                           \
        return;                                                                \
    }                                                                          \
    (*env)->ThrowNew(env, ra_class, info);                                     \
    return;                                                                    \
}

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_org_apache_teaclave_javasdk_host_TeeSdkEnclave_registerNatives(JNIEnv *env, jclass cls);

/*
 * Class:     org_apache_teaclave_javasdk_host_TeeSdkEnclave
 * Method:    nativeCreateEnclave
 * Signature: (ILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL JavaEnclave_TeeSDKSVMNativeCreateEnclave(JNIEnv *, jobject, jint, jstring);

/*
 * Class:     org_apache_teaclave_javasdk_host_TeeSdkEnclave
 * Method:    nativeSvmAttachIsolate
 * Signature: (JILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL JavaEnclave_TeeSDKSVMNativeSvmAttachIsolate(JNIEnv *, jobject, jlong, jint, jstring);

/*
 * Class:     org_apache_teaclave_javasdk_host_TeeSdkEnclave
 * Method:    nativeLoadService
 * Signature: (JJ[B)[B
 */
JNIEXPORT jbyteArray JNICALL JavaEnclave_TeeSDKSVMNativeLoadService(JNIEnv *, jobject, jlong, jlong, jbyteArray);

/*
 * Class:     org_apache_teaclave_javasdk_host_TeeSdkEnclave
 * Method:    nativeInvokeMethod
 * Signature: (JJ[B)[B
 */
JNIEXPORT jbyteArray JNICALL JavaEnclave_TeeSDKSVMNativeInvokeMethod(JNIEnv *, jobject, jlong, jlong, jbyteArray);

/*
 * Class:     org_apache_teaclave_javasdk_host_TeeSdkEnclave
 * Method:    nativeUnloadService
 * Signature: (JJ[B)[B
 */
JNIEXPORT jbyteArray JNICALL JavaEnclave_TeeSDKSVMNativeUnloadService(JNIEnv *, jobject, jlong, jlong, jbyteArray);

/*
 * Class:     org_apache_teaclave_javasdk_host_TeeSdkEnclave
 * Method:    nativeSvmDetachIsolate
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL JavaEnclave_TeeSDKSVMNativeSvmDetachIsolate(JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     org_apache_teaclave_javasdk_host_TeeSdkEnclave
 * Method:    nativeDestroyEnclave
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL JavaEnclave_TeeSDKSVMNativeDestroyEnclave(JNIEnv *, jobject, jlong);

/*
 * Class:     org_apache_teaclave_javasdk_host_TeeSdkEnclave
 * Method:    nativeGenerateAttestationReport
 * Signature: (J[B)Lorg/apache/teaclave/javasdk/host/TeeSdkAttestationReport;
 */
JNIEXPORT jobject JNICALL JavaEnclave_TeeSDK_REMOTE_ATTESTATION_REPORT(JNIEnv *, jobject, jlong, jbyteArray);

#ifdef __cplusplus
}
#endif
#endif