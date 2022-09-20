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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "sgx_quote_3.h"
#include "sgx_urts.h"
#include "sgx_pce.h"
#include "sgx_error.h"

#include <occlum_dcap.h>

#include "jni_occlum_attestation_generate.h"

static JNINativeMethod embedded_libos_occlum_enclave_methods[] = {
    {"generateAttestationReportNative",   LIBOS_OCCLUM_ENCLAVE_REMOTE_ATTESTATION_GENERATION_SIGNATURE,   (void *)&JavaEnclave_TeeLibOSNativeRemoteAttestationGenerate},
};

JNIEXPORT void JNICALL Java_org_apache_teaclave_javasdk_enclave_agent_RemoteAttestation_registerNatives(JNIEnv *env, jclass cls) {
    (*env)->RegisterNatives(env, cls, embedded_libos_occlum_enclave_methods, sizeof(embedded_libos_occlum_enclave_methods)/sizeof(embedded_libos_occlum_enclave_methods[0]));
}

JNIEXPORT jobject JNICALL JavaEnclave_TeeLibOSNativeRemoteAttestationGenerate(JNIEnv *env, jclass cls, jbyteArray userData) {

    void *handle = dcap_quote_open();
    if (handle == NULL) {
        THROW_EXCEPTION(env, LIBOS_OCCLUM_ENCLAVE_REMOTE_ATTESTATION_EXCEPTION, "libos enclave occlum remote attestation generate: dcap_quote_open failed.")
    }

    uint32_t quote_size = dcap_get_quote_size(handle);
    uint8_t *p_quote_buffer = (uint8_t*)malloc(quote_size);
    if (p_quote_buffer == NULL) {
        dcap_quote_close(handle);
        THROW_EXCEPTION(env, LIBOS_OCCLUM_ENCLAVE_REMOTE_ATTESTATION_EXCEPTION, "libos enclave occlum remote attestation generate: malloc p_quote_buffer failed.")
    }
    memset(p_quote_buffer, 0, quote_size);

    sgx_report_data_t report_data = {0};
    jbyte *userData_copy = (*env)->GetByteArrayElements(env, userData, NULL);
    int userData_copy_length = (*env)->GetArrayLength(env, userData);
    memcpy(report_data.d, userData_copy, userData_copy_length);

    int32_t ret = dcap_generate_quote(handle, p_quote_buffer, &report_data);
    if (ret != 0x0) {
        (*env)->ReleaseByteArrayElements(env, userData, userData_copy, 0);
        free(p_quote_buffer);
        dcap_quote_close(handle);
        THROW_EXCEPTION(env, LIBOS_OCCLUM_ENCLAVE_REMOTE_ATTESTATION_EXCEPTION, "libos enclave occlum remote attestation generate: dcap_generate_quote failed.")
    }

    sgx_quote3_t *p_quote = (sgx_quote3_t *)p_quote_buffer;
    sgx_report_body_t *p_rep_body = (sgx_report_body_t *)(&p_quote->report_body);

    jbyteArray quote_array = (*env)->NewByteArray(env, quote_size);
    jbyte *quote_array_ptr = (*env)->GetByteArrayElements(env, quote_array, NULL);
    memcpy(quote_array_ptr, p_quote_buffer, quote_size);

    // create mr enclave byte array.
    jbyteArray mr_enclave = (*env)->NewByteArray(env, SGX_HASH_SIZE);
    jbyte *mr_enclave_buf = (*env)->GetByteArrayElements(env, mr_enclave, NULL);
    memcpy(mr_enclave_buf, p_quote->report_body.mr_enclave.m, SGX_HASH_SIZE);

    // create mr signer byte array.
    jbyteArray mr_signer = (*env)->NewByteArray(env, SGX_HASH_SIZE);
    jbyte *mr_signer_buf = (*env)->GetByteArrayElements(env, mr_signer, NULL);
    memcpy(mr_signer_buf, p_quote->report_body.mr_signer.m, SGX_HASH_SIZE);

    // create user data byte array.
    jbyteArray user_data = (*env)->NewByteArray(env, SGX_REPORT_DATA_SIZE);
    jbyte *user_data_buf = (*env)->GetByteArrayElements(env, user_data, NULL);
    memcpy(user_data_buf, p_quote->report_body.report_data.d, SGX_REPORT_DATA_SIZE);

    (*env)->ReleaseByteArrayElements(env, userData, userData_copy, 0);
    (*env)->ReleaseByteArrayElements(env, quote_array, quote_array_ptr, 0);
    (*env)->ReleaseByteArrayElements(env, mr_enclave, mr_enclave_buf, 0);
    (*env)->ReleaseByteArrayElements(env, mr_signer, mr_signer_buf, 0);
    (*env)->ReleaseByteArrayElements(env, user_data, user_data_buf, 0);
    free(p_quote_buffer);
    dcap_quote_close(handle);

    jclass libos_inner_ra_report_clazz = (*env)->FindClass(env, LIBOS_OCCLUM_INNER_ATTESTATION_REPORT);
    jmethodID construct = (*env)->GetMethodID(env, libos_inner_ra_report_clazz, "<init>", "([B[B[B[B)V");
    return (*env)->NewObject(env, libos_inner_ra_report_clazz, construct, quote_array, mr_signer, mr_enclave, user_data);
}
