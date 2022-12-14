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
/* Header for class org_apache_teaclave_javasdk_enclave_EnclaveTestHelper */

#ifndef _Included_org_apache_teaclave_javasdk_enclave_EnclaveTestHelper
#define _Included_org_apache_teaclave_javasdk_enclave_EnclaveTestHelper
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_apache_teaclave_javasdk_enclave_EnclaveTestHelper
 * Method:    invokeEnclave
 * Signature: ([B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_org_apache_teaclave_javasdk_enclave_EnclaveTestHelper_invokeEnclave
  (JNIEnv *, jclass, jbyteArray);

/*
 * Class:     org_apache_teaclave_javasdk_enclave_EnclaveTestHelper
 * Method:    loadService
 * Signature: ([B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_org_apache_teaclave_javasdk_enclave_EnclaveTestHelper_loadService
  (JNIEnv *, jclass, jbyteArray);

/*
 * Class:     org_apache_teaclave_javasdk_enclave_EnclaveTestHelper
 * Method:    unloadService
 * Signature: ([B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_org_apache_teaclave_javasdk_enclave_EnclaveTestHelper_unloadService
  (JNIEnv *, jclass, jbyteArray);

/*
 * Class:     org_apache_teaclave_javasdk_enclave_EnclaveTestHelper
 * Method:    createIsolate
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_apache_teaclave_javasdk_enclave_EnclaveTestHelper_createIsolate__
  (JNIEnv *, jclass);

/*
 * Class:     org_apache_teaclave_javasdk_enclave_EnclaveTestHelper
 * Method:    createIsolate
 * Signature: ([Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_apache_teaclave_javasdk_enclave_EnclaveTestHelper_createIsolate___3Ljava_lang_String_2
  (JNIEnv *, jclass, jobjectArray);

/*
 * Class:     org_apache_teaclave_javasdk_enclave_EnclaveTestHelper
 * Method:    destroyIsolate
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_apache_teaclave_javasdk_enclave_EnclaveTestHelper_destroyIsolate
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif
