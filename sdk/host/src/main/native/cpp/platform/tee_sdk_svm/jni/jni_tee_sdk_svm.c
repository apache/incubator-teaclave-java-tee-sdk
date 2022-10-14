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

#include <assert.h>
#include <limits.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <dlfcn.h>

#include <sgx_urts.h>

#include <graal_isolate.h>
#include <enc_environment.h>
#include <enc_exported_symbol.h>

#include "tee_sdk_enclave_u.h"
#include "jni_tee_sdk_svm.h"

static JNINativeMethod tee_sdk_svm_methods[] = {
    {"nativeCreateEnclave",             "(ILjava/lang/String;)I",                    (void *)&JavaEnclave_TeeSDKSVMNativeCreateEnclave},
    {"nativeSvmAttachIsolate",          "(JILjava/lang/String;)I",                   (void *)&JavaEnclave_TeeSDKSVMNativeSvmAttachIsolate},
    {"nativeLoadService",               TEE_SDK_SVM_NATIVE_CALL_SIGNATURE,           (void *)&JavaEnclave_TeeSDKSVMNativeLoadService},
    {"nativeInvokeMethod",              TEE_SDK_SVM_NATIVE_CALL_SIGNATURE,           (void *)&JavaEnclave_TeeSDKSVMNativeInvokeMethod},
    {"nativeUnloadService",             TEE_SDK_SVM_NATIVE_CALL_SIGNATURE,           (void *)&JavaEnclave_TeeSDKSVMNativeUnloadService},
    {"nativeSvmDetachIsolate",          "(JJ)I",                                     (void *)&JavaEnclave_TeeSDKSVMNativeSvmDetachIsolate},
    {"nativeDestroyEnclave",            "(J)I",                                      (void *)&JavaEnclave_TeeSDKSVMNativeDestroyEnclave},
    {"nativeGenerateAttestationReport", TEE_SDK_REMOTE_ATTESTATION_REPORT_SIGNATURE, (void *)&JavaEnclave_TeeSDK_REMOTE_ATTESTATION_REPORT},
};

JNIEXPORT void JNICALL
Java_org_apache_teaclave_javasdk_host_TeeSdkEnclave_registerNatives(JNIEnv *env, jclass cls) {
    (*env)->RegisterNatives(env, cls, tee_sdk_svm_methods, sizeof(tee_sdk_svm_methods)/sizeof(tee_sdk_svm_methods[0]));
}

void set_long_field_value(JNIEnv *env, jclass class_mirror, jobject obj, const char *field_name, jlong value) {
    jfieldID field_id = (*env)->GetFieldID(env, class_mirror, field_name, "J");
    (*env)->SetLongField(env, obj, field_id, value);
}

void set_int_field_value(JNIEnv *env, jclass class_mirror, jobject obj, const char *field_name, jint value) {
    jfieldID field_id = (*env)->GetFieldID(env, class_mirror, field_name, "I");
    (*env)->SetIntField(env, obj, field_id, value);
}

enclave_calling_stub_result enclave_calling_entry(JNIEnv *env, jlong enclave_handler, jlong isolate_handler, jbyteArray payload, enclave_calling_stub stub) {
    jbyte *payload_copy = (*env)->GetByteArrayElements(env, payload, NULL);
    int payload_copy_length = (*env)->GetArrayLength(env, payload);

    enc_data_t input;
    input.data = (char*)payload_copy;
    input.data_len = payload_copy_length;
    enc_data_t output;
    output.data = NULL;
    output.data_len = 0x0;

    jbyteArray invocation_result_array;
    enclave_calling_stub_result result_wrapper;
    result_wrapper.ret = 0;
    result_wrapper.result = invocation_result_array;

    stub(enclave_handler, &result_wrapper.ret, (graal_isolate_t*)isolate_handler, (void*)(input.data), (size_t)(input.data_len), (void*)(&(output.data)), (size_t*)(&(output.data_len)));
    if (result_wrapper.ret != 0) {
        (*env)->ReleaseByteArrayElements(env, payload, payload_copy, 0);
        // free buffer malloc in native image by callback mechanism.
        free(output.data);
        return result_wrapper;
    }

    // create a byte array.
    invocation_result_array = (*env)->NewByteArray(env, output.data_len);
    jbyte *invocation_result_array_ptr = (*env)->GetByteArrayElements(env, invocation_result_array, NULL);
    memcpy(invocation_result_array_ptr, output.data, (size_t)output.data_len);

    (*env)->ReleaseByteArrayElements(env, payload, payload_copy, 0);
    // free buffer malloc in jni.
    (*env)->ReleaseByteArrayElements(env, invocation_result_array, invocation_result_array_ptr, 0);
    // free buffer malloc in native image by callback mechanism.
    free(output.data);

    result_wrapper.result = invocation_result_array;
    return result_wrapper;
}

JNIEXPORT jint JNICALL
JavaEnclave_TeeSDKSVMNativeCreateEnclave(JNIEnv *env, jobject obj, jint mode, jstring path) {
    // set enclave' debug mode enable_debug_mode.
    // mode = 0, is SGX_RELEASE_FLAG
    // mode = 1, is SGX_DEBUG_FLAG
    int enable_debug_mode = 0;
    if (mode == 1) {
        enable_debug_mode = (int)SGX_DEBUG_FLAG;
    }

    // create a tee sdk sgx enclave instance.
    const char *path_str = (path == 0) ? 0 : (*env)->GetStringUTFChars(env, path, 0);
    sgx_enclave_id_t enclave_id;
    int ret = sgx_create_enclave(path_str, enable_debug_mode, NULL, NULL, &enclave_id, NULL);
    (*env)->ReleaseStringUTFChars(env, path, path_str);
    if (ret != SGX_SUCCESS) {
        THROW_EXCEPTION(env, ENCLAVE_CREATING_EXCEPTION, "create tee sdk enclave by native calling failed.")
    }

    // set enclave_handler back to TeeSdkEnclave.enclaveHandle field.
    jclass enclave_class = (*env)->GetObjectClass(env, obj);
    set_long_field_value(env, enclave_class, obj, "enclaveHandle", (jlong)enclave_id);

    return 0;
}

JNIEXPORT jint JNICALL
JavaEnclave_TeeSDKSVMNativeSvmAttachIsolate(JNIEnv *env, jobject obj, jlong enclave_handler, jint flag, jstring args) {
    // create an isolate in enclave.
    uint64_t isolate = 0;
    uint64_t isolateThread = 0;
    int ret = 0;

    char *args_str = (*env)->GetStringUTFChars(env, args, 0);
    enclave_svm_isolate_create((size_t)enclave_handler, &ret, (void *)(&isolate), (void *)(&isolateThread), flag, args_str);
    if (ret != 0) {
        (*env)->ReleaseStringUTFChars(env, args, args_str);
        THROW_EXCEPTION(env, ENCLAVE_CREATING_EXCEPTION, "attach native svm failed when creating an enclave.")
    }

    (*env)->ReleaseStringUTFChars(env, args, args_str);
    jclass enclave_class = (*env)->GetObjectClass(env, obj);
    // set isolate back to isolateHandle field.
    set_long_field_value(env, enclave_class, obj, "isolateHandle", (jlong)isolate);
    // set isolateThread back to isolateThreadHandle field.
    set_long_field_value(env, enclave_class, obj, "isolateThreadHandle", (jlong)isolateThread);

    return ret;
}

JNIEXPORT jbyteArray JNICALL
JavaEnclave_TeeSDKSVMNativeLoadService(JNIEnv *env, jobject obj, jlong enclave_handler, jlong isolate_handler, jbyteArray load_service_payload) {
    enclave_calling_stub_result result_wrapper = enclave_calling_entry(env, enclave_handler, isolate_handler, load_service_payload, (enclave_calling_stub) load_enclave_svm_services);
    if (result_wrapper.ret != 0) {
        THROW_EXCEPTION(env, ENCLAVE_SERVICE_LOADING_EXCEPTION, "tee sdk service loading native call failed.")
    }
    return result_wrapper.result;
}

JNIEXPORT jbyteArray JNICALL
JavaEnclave_TeeSDKSVMNativeInvokeMethod(JNIEnv *env, jobject obj, jlong enclave_handler, jlong isolate_handler, jbyteArray invoke_service_payload) {
    enclave_calling_stub_result result_wrapper = enclave_calling_entry(env, enclave_handler, isolate_handler, invoke_service_payload, (enclave_calling_stub) invoke_enclave_svm_service);
    if (result_wrapper.ret != 0) {
        THROW_EXCEPTION(env, ENCLAVE_SERVICE_INVOKING_EXCEPTION, "tee sdk service method invoking native call failed.")
    }
    return result_wrapper.result;
}

JNIEXPORT jbyteArray JNICALL
JavaEnclave_TeeSDKSVMNativeUnloadService(JNIEnv *env, jobject obj, jlong enclave_handler, jlong isolate_handler, jbyteArray unload_service_payload) {
    enclave_calling_stub_result result_wrapper = enclave_calling_entry(env, enclave_handler, isolate_handler, unload_service_payload, (enclave_calling_stub) unload_enclave_svm_service);
    if (result_wrapper.ret != 0) {
        THROW_EXCEPTION(env, ENCLAVE_SERVICE_UNLOADING_EXCEPTION, "tee sdk service unloading native call failed.")
    }
    return result_wrapper.result;
}

JNIEXPORT jint JNICALL
JavaEnclave_TeeSDKSVMNativeSvmDetachIsolate(JNIEnv *env, jobject obj, jlong enclave_handler, jlong isolate_thread_handler) {
    int ret = 0;
    enclave_svm_isolate_destroy((sgx_enclave_id_t)enclave_handler, &ret, (uint64_t)isolate_thread_handler);
    if (ret != 0) {
        THROW_EXCEPTION(env, ENCLAVE_DESTROYING_EXCEPTION, "isolate destroy native call failed.")
    }
    return ret;
}

JNIEXPORT jint JNICALL
JavaEnclave_TeeSDKSVMNativeDestroyEnclave(JNIEnv *env, jobject obj, jlong enclave_handler) {
    if ((jint)sgx_destroy_enclave((sgx_enclave_id_t)enclave_handler) != 0) {
        THROW_EXCEPTION(env, ENCLAVE_DESTROYING_EXCEPTION, "enclave destroy native call failed.")
    }
    return 0;
}

JNIEXPORT jobject JNICALL
JavaEnclave_TeeSDK_REMOTE_ATTESTATION_REPORT(JNIEnv *env, jobject obj, jlong enclave_handler, jbyteArray data) {
    int ret = 0;
    quote3_error_t qe3_ret = SGX_QL_SUCCESS;
    // Step one, load remote attestation related .signed files.
    if (SGX_QL_SUCCESS != (qe3_ret = load_qe_signed_package())) {
        THROW_EXCEPTION(env, REMOTE_ATTESTATION_CLASS_NAME, "load remote attestation related .signed files failed")
    }

    // Step two, generate target enclave's report info.
    sgx_report_t ra_report;
    jbyte *data_copy = (*env)->GetByteArrayElements(env, data, NULL);
    int length = (*env)->GetArrayLength(env, data);
    generate_remote_attestation_report(enclave_handler, &ret, (void *)data_copy, (size_t)length, &ra_report);
    if (ret != 0) {
        (*env)->ReleaseByteArrayElements(env, data, data_copy, 0);
        THROW_EXCEPTION(env, REMOTE_ATTESTATION_CLASS_NAME, "generate target enclave's report info failed")
    }

    // Step three, get quote size.
    uint32_t quote_size = 0;
    qe3_ret = sgx_qe_get_quote_size(&quote_size);
    if (SGX_QL_SUCCESS != qe3_ret) {
        (*env)->ReleaseByteArrayElements(env, data, data_copy, 0);
        THROW_EXCEPTION(env, REMOTE_ATTESTATION_CLASS_NAME, "get quote size failed")
    }

    // Step four, get quote data from target enclave's report.
    // quote_buffer_ptr will store sgx_quote3_t struct data.
    uint8_t* quote_buffer_ptr = NULL;
    quote_buffer_ptr = (uint8_t*)malloc(quote_size);
    if (NULL == quote_buffer_ptr) {
        (*env)->ReleaseByteArrayElements(env, data, data_copy, 0);
        THROW_EXCEPTION(env, REMOTE_ATTESTATION_CLASS_NAME, "get quote temp heap for target enclave's report failed")
    }
    memset(quote_buffer_ptr, 0, quote_size);

    qe3_ret = sgx_qe_get_quote(&ra_report, quote_size, quote_buffer_ptr);
    if (SGX_QL_SUCCESS != qe3_ret) {
        if (NULL != quote_buffer_ptr) {
            free(quote_buffer_ptr);
        }
        (*env)->ReleaseByteArrayElements(env, data, data_copy, 0);
        THROW_EXCEPTION(env, REMOTE_ATTESTATION_CLASS_NAME ,"get quote data from target enclave's report failed")
    }

    // Step five, clear up loaded qe.
    qe3_ret = unload_qe_signed_package();
    if (SGX_QL_SUCCESS != qe3_ret) {
        printf("Teaclave Java TEE SDK Warning: clear up loaded qe files failed");
    }

    // create a quote byte array.
    jbyteArray quote_array = (*env)->NewByteArray(env, quote_size);
    jbyte *quote_array_ptr = (*env)->GetByteArrayElements(env, quote_array, NULL);
    memcpy(quote_array_ptr, quote_buffer_ptr, (size_t)quote_size);

    // create mr enclave byte array.
    jbyteArray mr_enclave = (*env)->NewByteArray(env, SGX_HASH_SIZE);
    jbyte *mr_enclave_buf = (*env)->GetByteArrayElements(env, mr_enclave, NULL);
    memcpy(mr_enclave_buf, ra_report.body.mr_enclave.m, SGX_HASH_SIZE);

    // create mr signer byte array.
    jbyteArray mr_signer = (*env)->NewByteArray(env, SGX_HASH_SIZE);
    jbyte *mr_signer_buf = (*env)->GetByteArrayElements(env, mr_signer, NULL);
    memcpy(mr_signer_buf, ra_report.body.mr_signer.m, SGX_HASH_SIZE);

    // create user data byte array.
    jbyteArray user_data = (*env)->NewByteArray(env, SGX_REPORT_DATA_SIZE);
    jbyte *user_data_buf = (*env)->GetByteArrayElements(env, user_data, NULL);
    memcpy(user_data_buf, ra_report.body.report_data.d, SGX_REPORT_DATA_SIZE);

    (*env)->ReleaseByteArrayElements(env, data, data_copy, 0);
    (*env)->ReleaseByteArrayElements(env, quote_array, quote_array_ptr, 0);
    (*env)->ReleaseByteArrayElements(env, mr_enclave, mr_enclave_buf, 0);
    (*env)->ReleaseByteArrayElements(env, mr_signer, mr_signer_buf, 0);
    (*env)->ReleaseByteArrayElements(env, user_data, user_data_buf, 0);
    free(quote_buffer_ptr);

    jclass tee_sdk_ra_report_clazz = (*env)->FindClass(env, TEE_SDK_REMOTE_ATTESTATION_REPORT_CLASS_NAME);
    jmethodID construct = (*env)->GetMethodID(env, tee_sdk_ra_report_clazz, "<init>", "([B[B[B[B)V");
    return (*env)->NewObject(env, tee_sdk_ra_report_clazz, construct, quote_array, mr_signer, mr_enclave, user_data);
}