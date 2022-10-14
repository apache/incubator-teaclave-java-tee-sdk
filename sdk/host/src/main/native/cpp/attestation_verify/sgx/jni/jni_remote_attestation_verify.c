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

#include "jni_remote_attestation_verify.h"

#define QUOTE_VERIFICATION_STATUS_SUCCESS                 0
#define QUOTE_VERIFICATION_STATUS_GET_DATA_SIZE_FAILED    1
#define QUOTE_VERIFICATION_STATUS_QUOTE_VERIFY_FAILED     2
#define QUOTE_VERIFICATION_STATUS_MEMORY_MALLOC_FAILED    3
#define QUOTE_VERIFICATION_VERSION_CHECK_SUCCESS          0
#define QUOTE_VERIFICATION_VERSION_CHECK_FAILED          -1
#define QUOTE_VERIFICATION_SUCCESS                        0
#define QUOTE_VERIFICATION_OUT_OF_DATA                    1
#define QUOTE_VERIFICATION_NO_TERMINAL                    2
#define QUOTE_VERIFICATION_FAILED_WITH_TERMINAL           3

static JNINativeMethod sgx_remote_attestation_verify_methods[] = {
    {"nativeVerifyAttestationReport", SGX_ENCLAVE_REMOTE_ATTESTATION_VERIFY_SIGNATURE, (void *)&JavaEnclave_SGX_ENCLAVE_REMOTE_ATTESTATION_VERIFY},
};

void set_int_field_value(JNIEnv *env, jclass class_mirror, jobject obj, const char *field_name, jint value) {
    jfieldID field_id = (*env)->GetFieldID(env, class_mirror, field_name, "I");
    (*env)->SetIntField(env, obj, field_id, value);
}

verify_result_wrapper ecdsa_quote_verification_qvl(const uint8_t* quote, uint32_t length) {
    verify_result_wrapper result;
    result.status = QUOTE_VERIFICATION_STATUS_SUCCESS;
    result.version_check = QUOTE_VERIFICATION_VERSION_CHECK_SUCCESS;
    result.verify_flag = QUOTE_VERIFICATION_SUCCESS;

    quote3_error_t dcap_ret = SGX_QL_ERROR_UNEXPECTED;
    uint32_t supplemental_data_size = 0;
    uint8_t *p_supplemental_data = NULL;
    time_t current_time = 0;
    uint32_t collateral_expiration_status = 1;
    sgx_ql_qv_result_t quote_verification_result = SGX_QL_QV_RESULT_UNSPECIFIED;

    // Step one, get supplemental_data_size.
    dcap_ret = sgx_qv_get_quote_supplemental_data_size(&supplemental_data_size);
    if (dcap_ret != SGX_QL_SUCCESS) {
        // printf("Teaclave Java TEE SDK Remote Attestation Error: sgx_qv_get_quote_supplemental_data_size failed: 0x%04x\n", dcap_ret);
        result.status = QUOTE_VERIFICATION_STATUS_GET_DATA_SIZE_FAILED;
        return result;
    }
    if (supplemental_data_size != sizeof(sgx_ql_qv_supplemental_t)) {
        // printf("Teaclave Java TEE SDK Remote Attestation Warning: sgx_qv_get_quote_supplemental_data_size returned size is not same with header definition in SGX SDK, please make sure you are using same version of SGX SDK and DCAP QVL.\n");
        result.version_check = QUOTE_VERIFICATION_VERSION_CHECK_FAILED;
        return result;
    }

    p_supplemental_data = (uint8_t*)malloc(supplemental_data_size);
    if (p_supplemental_data != NULL) {
        memset(p_supplemental_data, 0, sizeof(supplemental_data_size));
    } else {
        result.status = QUOTE_VERIFICATION_STATUS_MEMORY_MALLOC_FAILED;
        return result;
    }

    current_time = time(NULL);
    dcap_ret = sgx_qv_verify_quote(
        quote, length, NULL,
        current_time, &collateral_expiration_status,
        &quote_verification_result, NULL,
        supplemental_data_size, p_supplemental_data);

    free(p_supplemental_data);

    if (dcap_ret != SGX_QL_SUCCESS) {
        result.status = QUOTE_VERIFICATION_STATUS_QUOTE_VERIFY_FAILED;
        return result;
    }

    switch (quote_verification_result) {
        case SGX_QL_QV_RESULT_OK:
            if (collateral_expiration_status == 0) {
                // Verification completed successfully.
                result.verify_flag = QUOTE_VERIFICATION_SUCCESS;
            } else {
                // Verification completed, but collateral is out of date based on 'expiration_check_date' you provided.
                result.verify_flag = QUOTE_VERIFICATION_OUT_OF_DATA;
            }
            break;
        case SGX_QL_QV_RESULT_CONFIG_NEEDED:
        case SGX_QL_QV_RESULT_OUT_OF_DATE:
        case SGX_QL_QV_RESULT_OUT_OF_DATE_CONFIG_NEEDED:
        case SGX_QL_QV_RESULT_SW_HARDENING_NEEDED:
        case SGX_QL_QV_RESULT_CONFIG_AND_SW_HARDENING_NEEDED:
            // Verification completed with Non-terminal result, you could view value of quote_verification_result for more info.
            result.verify_flag = QUOTE_VERIFICATION_NO_TERMINAL;
            break;
        case SGX_QL_QV_RESULT_INVALID_SIGNATURE:
        case SGX_QL_QV_RESULT_REVOKED:
        case SGX_QL_QV_RESULT_UNSPECIFIED:
        default:
            // Verification completed with Terminal result, you could view value of quote_verification_result for more info.
            result.verify_flag = QUOTE_VERIFICATION_FAILED_WITH_TERMINAL;
            break;
    }
    return result;
}

JNIEXPORT void JNICALL Java_org_apache_teaclave_javasdk_host_SGXRemoteAttestationVerify_registerNatives(JNIEnv *env, jclass cls) {
    (*env)->RegisterNatives(env, cls, sgx_remote_attestation_verify_methods, sizeof(sgx_remote_attestation_verify_methods)/sizeof(sgx_remote_attestation_verify_methods[0]));
}

JNIEXPORT jint JNICALL
JavaEnclave_SGX_ENCLAVE_REMOTE_ATTESTATION_VERIFY(JNIEnv *env, jclass mirror, jbyteArray quote, jobject jResult) {
    jbyte *quote_copy = (*env)->GetByteArrayElements(env, quote, NULL);
    int quote_length = (*env)->GetArrayLength(env, quote);
    verify_result_wrapper result = ecdsa_quote_verification_qvl(quote_copy, quote_length);
    (*env)->ReleaseByteArrayElements(env, quote, quote_copy, 0);

    jclass j_result_class = (*env)->GetObjectClass(env, jResult);
    set_int_field_value(env, j_result_class, jResult, "status", (jint)result.status);
    set_int_field_value(env, j_result_class, jResult, "versionCheck", (jint)result.version_check);
    set_int_field_value(env, j_result_class, jResult, "verifyFlag", (jint)result.verify_flag);

    return 0;
}