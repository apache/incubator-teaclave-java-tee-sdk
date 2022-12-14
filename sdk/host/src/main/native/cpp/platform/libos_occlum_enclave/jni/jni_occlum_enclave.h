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

/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_apache_teaclave_javasdk_host_EmbeddedLibOSEnclave */

#ifndef _Included_org_apache_teaclave_javasdk_host_EmbeddedLibOSEnclave
#define _Included_org_apache_teaclave_javasdk_host_EmbeddedLibOSEnclave
#ifdef __cplusplus
extern "C" {
#endif

#define ENCLAVE_CREATING_SIGNATURE         "(IIIILorg/apache/teaclave/javasdk/host/EmbeddedLibOSEnclaveConfigure;Ljava/lang/String;)I"
#define ENCLAVE_CREATING_EXCEPTION         "org/apache/teaclave/javasdk/host/exception/EnclaveCreatingException"
#define ENCLAVE_DESTROYING_EXCEPTION       "org/apache/teaclave/javasdk/host/exception/EnclaveDestroyingException"

typedef struct {
    jstring  handler;
    char*    handler_str;
} t_jvm_args;

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

/*
 * Class:     org_apache_teaclave_javasdk_host_EmbeddedLibOSEnclave
 * Method:    registerNatives
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_apache_teaclave_javasdk_host_EmbeddedLibOSEnclave_registerNatives
  (JNIEnv *, jclass);

/*
 * Class:     org_apache_teaclave_javasdk_host_EmbeddedLibOSEnclave
 * Method:    nativeCreateEnclave
 * Signature: (IIIILorg/apache/teaclave/javasdk/host/EmbeddedLibOSEnclaveConfigure;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL JavaEnclave_TeeLibOSNativeCreateEnclave
  (JNIEnv *, jobject, jint, jint, jint, jint, jobject, jstring);

/*
 * Class:     org_apache_teaclave_javasdk_host_EmbeddedLibOSEnclave
 * Method:    nativeDestroyEnclave
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL JavaEnclave_TeeLibOSNativeDestroyEnclave
  (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif