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

typedef void (*enclave_calling_stub)(jlong, int*, graal_isolate_t*, void*, size_t, void*, size_t*);

static JNINativeMethod tee_sdk_svm_methods[] = {
    {"nativeCreateEnclave",       "(ILjava/lang/String;)I",          (void *)&JavaEnclave_TeeSDKSVMNativeCreateEnclave},
    {"nativeSvmAttachIsolate",    "(J)I",                            (void *)&JavaEnclave_TeeSDKSVMNativeSvmAttachIsolate},
    {"nativeLoadService",         TEE_SDK_SVM_NATIVE_CALL_SIGNATURE, (void *)&JavaEnclave_TeeSDKSVMNativeLoadService},
    {"nativeInvokeMethod",        TEE_SDK_SVM_NATIVE_CALL_SIGNATURE, (void *)&JavaEnclave_TeeSDKSVMNativeInvokeMethod},
    {"nativeUnloadService",       TEE_SDK_SVM_NATIVE_CALL_SIGNATURE, (void *)&JavaEnclave_TeeSDKSVMNativeUnloadService},
    {"nativeSvmDetachIsolate",    "(JJ)I",                           (void *)&JavaEnclave_TeeSDKSVMNativeSvmDetachIsolate},
    {"nativeDestroyEnclave",      "(J)I",                            (void *)&JavaEnclave_TeeSDKSVMNativeDestroyEnclave},
};

JNIEXPORT void JNICALL
Java_com_alibaba_confidentialcomputing_host_TeeSdkEnclave_registerNatives(JNIEnv *env, jclass cls) {
    (*env)->RegisterNatives(env, cls, tee_sdk_svm_methods, sizeof(tee_sdk_svm_methods)/sizeof(tee_sdk_svm_methods[0]));
}

void set_long_field_value(JNIEnv *env, jclass class_mirror, jobject obj, const char *field_name, jlong value) {
    jfieldID field_id = (*env)->GetFieldID(env, class_mirror, field_name, "J");
    (*env)->SetLongField(env, obj, field_id, value);
}

jobject build_invocation_result(JNIEnv *env, jint ret, jbyteArray array) {
    // build jni return object InnerNativeInvocationResult.
    jclass invocation_result_clazz = (*env)->FindClass(env, TEE_SDK_SVM_RETURN_OBJECT_SIGNATURE);
    jmethodID id = (*env)->GetMethodID(env, invocation_result_clazz, "<init>", "(I[B)V");
    return (*env)->NewObject(env, invocation_result_clazz, id, (jint)ret, array);
}

jobject enclave_calling_entry(JNIEnv *env, jlong enclave_handler, jlong isolate_handler, jbyteArray payload, enclave_calling_stub stub) {
    jbyte *payload_copy = (*env)->GetByteArrayElements(env, payload, NULL);
    int payload_copy_length = (*env)->GetArrayLength(env, payload);

    enc_data_t input;
    input.data = (char*)payload_copy;
    input.data_len = payload_copy_length;
    enc_data_t output;
    output.data = NULL;
    output.data_len = 0x0;

    int ret = 0x0;
    stub(enclave_handler, &ret, (graal_isolate_t*)isolate_handler, (void*)(input.data), (size_t)(input.data_len), (void*)(&(output.data)), (size_t*)(&(output.data_len)));

    // create a byte array.
    jbyteArray invocation_result_array = (*env)->NewByteArray(env, output.data_len);
    jbyte *invocation_result_array_ptr = (*env)->GetByteArrayElements(env, invocation_result_array, NULL);
    memcpy(invocation_result_array_ptr, output.data, (size_t)output.data_len);

    (*env)->ReleaseByteArrayElements(env, payload, payload_copy, 0);
    // free buffer malloc in jni.
    (*env)->ReleaseByteArrayElements(env, invocation_result_array, invocation_result_array_ptr, 0);
    // free buffer malloc in native image by callback mechanism.
    free(output.data);

    return build_invocation_result(env, ret, invocation_result_array);
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
        return (int)ret;
    }

    // set enclave_handler back to TeeSdkEnclave.enclaveHandle field.
    jclass enclave_class = (*env)->GetObjectClass(env, obj);
    set_long_field_value(env, enclave_class, obj, "enclaveHandle", (jlong)enclave_id);

    return 0;
}

JNIEXPORT jint JNICALL
JavaEnclave_TeeSDKSVMNativeSvmAttachIsolate(JNIEnv *env, jobject obj, jlong enclave_handler) {
    // create an isolate in enclave.
    uint64_t isolate = 0;
    uint64_t isolateThread = 0;
    int ret = 0;
    enclave_svm_isolate_create((size_t)enclave_handler, &ret, (void *)(&isolate), (void *)(&isolateThread));

    jclass enclave_class = (*env)->GetObjectClass(env, obj);
    // set isolate back to isolateHandle field.
    set_long_field_value(env, enclave_class, obj, "isolateHandle", (jlong)isolate);
    // set isolateThread back to isolateThreadHandle field.
    set_long_field_value(env, enclave_class, obj, "isolateThreadHandle", (jlong)isolateThread);

    return ret;
}

JNIEXPORT jobject JNICALL
JavaEnclave_TeeSDKSVMNativeLoadService(JNIEnv *env, jobject obj, jlong enclave_handler, jlong isolate_handler, jbyteArray load_service_payload) {
    return enclave_calling_entry(env, enclave_handler, isolate_handler, load_service_payload, (enclave_calling_stub) load_enclave_svm_services);
}

JNIEXPORT jobject JNICALL
JavaEnclave_TeeSDKSVMNativeInvokeMethod(JNIEnv *env, jobject obj, jlong enclave_handler, jlong isolate_handler, jbyteArray invoke_service_payload) {
    return enclave_calling_entry(env, enclave_handler, isolate_handler, invoke_service_payload, (enclave_calling_stub) invoke_enclave_svm_service);
}

JNIEXPORT jobject JNICALL
JavaEnclave_TeeSDKSVMNativeUnloadService(JNIEnv *env, jobject obj, jlong enclave_handler, jlong isolate_handler, jbyteArray unload_service_payload) {
    return enclave_calling_entry(env, enclave_handler, isolate_handler, unload_service_payload, (enclave_calling_stub) unload_enclave_svm_service);
}

JNIEXPORT jint JNICALL
JavaEnclave_TeeSDKSVMNativeSvmDetachIsolate(JNIEnv *env, jobject obj, jlong enclave_handler, jlong isolate_thread_handler) {
    int ret = 0x0;
    enclave_svm_isolate_destroy((sgx_enclave_id_t)enclave_handler, &ret, (uint64_t)isolate_thread_handler);
    return ret;
}

JNIEXPORT jint JNICALL
JavaEnclave_TeeSDKSVMNativeDestroyEnclave(JNIEnv *env, jobject obj, jlong enclave_handler) {
    return (jint)sgx_destroy_enclave((sgx_enclave_id_t)enclave_handler);
}