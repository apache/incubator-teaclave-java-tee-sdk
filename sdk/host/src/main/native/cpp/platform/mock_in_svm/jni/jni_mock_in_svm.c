#include <assert.h>
#include <limits.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <dlfcn.h>

#include <graal_isolate.h>
#include <enc_environment.h>
#include <enc_exported_symbol.h>

#include "jni_mock_in_svm.h"

typedef int (*java_enclave_stub)(graal_isolate_t*, enc_data_t*, enc_data_t*, callbacks_t*);

static JNINativeMethod mock_in_svm_methods[] = {
    {"nativeCreateEnclave",       "(Ljava/lang/String;)I",           (void *)&JavaEnclave_MockSVMNativeCreateEnclave},
    {"nativeSvmAttachIsolate",    "(J)I",                            (void *)&JavaEnclave_MockSVMNativeSvmAttachIsolate},
    {"nativeLoadService",         MOCK_IN_SVM_NATIVE_CALL_SIGNATURE, (void *)&JavaEnclave_MockSVMNativeLoadService},
    {"nativeInvokeMethod",        MOCK_IN_SVM_NATIVE_CALL_SIGNATURE, (void *)&JavaEnclave_MockSVMNativeInvokeMethod},
    {"nativeUnloadService",       MOCK_IN_SVM_NATIVE_CALL_SIGNATURE, (void *)&JavaEnclave_MockSVMNativeUnloadService},
    {"nativeSvmDetachIsolate",    "(JJ)I",                           (void *)&JavaEnclave_MockSVMNativeSvmDetachIsolate},
    {"nativeDestroyEnclave",      "(J)I",                            (void *)&JavaEnclave_MockSVMNativeDestroyEnclave},
};

static void *mock_in_svm_load_service_symbol   = NULL;
static void *mock_in_svm_invoke_service_symbol = NULL;
static void *mock_in_svm_unload_service_symbol = NULL;

JNIEXPORT void JNICALL
Java_com_alibaba_confidentialcomputing_host_MockInSvmEnclave_registerNatives(JNIEnv *env, jclass cls) {
    (*env)->RegisterNatives(env, cls, mock_in_svm_methods, sizeof(mock_in_svm_methods)/sizeof(mock_in_svm_methods[0]));
}

void set_long_field_value(JNIEnv *env, jclass class_mirror, jobject obj, const char *field_name, jlong value) {
    jfieldID field_id = (*env)->GetFieldID(env, class_mirror, field_name, "J");
    (*env)->SetLongField(env, obj, field_id, value);
}

char* memcpy_char_pointer(char* src, int len) {
    char *ptr = malloc(len);
    memcpy(ptr, src, len);
    return (char*)ptr;
}

jobject build_invocation_result(JNIEnv *env, jint ret, jbyteArray array) {
    // build jni return object InnerNativeInvocationResult.
    jclass invocation_result_clazz = (*env)->FindClass(env, MOCK_IN_SVM_RETURN_OBJECT_SIGNATURE);
    jmethodID id = (*env)->GetMethodID(env, invocation_result_clazz, "<init>", "(I[B)V");
    return (*env)->NewObject(env, invocation_result_clazz, id, (jint)ret, array);
}

jobject service_operate_common(JNIEnv *env, jlong isolate_handler, jbyteArray payload, java_enclave_stub p_function) {
    jbyte *service_payload_copy = (*env)->GetByteArrayElements(env, payload, NULL);
    int service_payload_copy_length = (*env)->GetArrayLength(env, payload);
    enc_data_t invoke_data;
    invoke_data.data = (char*)service_payload_copy;
    invoke_data.data_len = service_payload_copy_length;
    enc_data_t result;
    result.data = NULL;
    result.data_len = 0x0;
    callbacks_t callback_methods;
    callback_methods.memcpy_char_pointer = &memcpy_char_pointer;
    callback_methods.exception_handler = NULL;
    int ret = p_function((graal_isolate_t*)isolate_handler, &invoke_data, &result, &callback_methods);
    (*env)->ReleaseByteArrayElements(env, payload, service_payload_copy, 0);

    // create a byte array.
    jbyteArray invocation_result_arr = (*env)->NewByteArray(env, result.data_len);
    jbyte *invocation_result_arr_point = (*env)->GetByteArrayElements(env, invocation_result_arr, NULL);
    memcpy(invocation_result_arr_point, result.data, result.data_len);

    // free buffer malloc in jni.
    (*env)->ReleaseByteArrayElements(env, invocation_result_arr, invocation_result_arr_point, 0);
    // free buffer malloc in native image by callback mechanism.
    free(result.data);

    return build_invocation_result(env, ret, invocation_result_arr);
}

JNIEXPORT jint JNICALL JavaEnclave_MockSVMNativeCreateEnclave(JNIEnv *env, jobject obj, jstring path) {
    const char *path_str = (path == 0) ? 0 : (*env)->GetStringUTFChars(env, path, 0);
    void *enclave_handler = dlopen(path_str , RTLD_LOCAL | RTLD_LAZY);
    if (enclave_handler == 0x0) {
        fprintf(stderr, "mock in svm dlopen error:%s\n", dlerror());
        return -1;
    }
    // find load service symbol.
    mock_in_svm_load_service_symbol = dlsym((void *)enclave_handler, "java_loadservice_invoke");
    if (!mock_in_svm_load_service_symbol) {
        fprintf(stderr, "java_loadservice_invoke error:%s\n", dlerror());
        dlclose(enclave_handler);
        return -1;
    }
    // find invoke service symbol.
    mock_in_svm_invoke_service_symbol = dlsym((void *)enclave_handler, "java_enclave_invoke");
    if (!mock_in_svm_invoke_service_symbol) {
        fprintf(stderr, "mock_in_svm_invoke_service_symbol error:%s\n", dlerror());
        dlclose(enclave_handler);
        return -1;
    }
    // find unload service symbol.
    mock_in_svm_unload_service_symbol = dlsym((void *)enclave_handler, "java_unloadservice_invoke");
    if (!mock_in_svm_unload_service_symbol) {
        fprintf(stderr, "mock_in_svm_unload_service_symbol error:%s\n", dlerror());
        dlclose(enclave_handler);
        return -1;
    }
    // set enclave_handler back to MockInSvmEnclave.enclaveSvmSdkHandle field.
    jclass class_enclave = (*env)->GetObjectClass(env, obj);
    set_long_field_value(env, class_enclave, obj, "enclaveSvmSdkHandle", (jlong)enclave_handler);
    return 0;
}

JNIEXPORT jint JNICALL JavaEnclave_MockSVMNativeSvmAttachIsolate(JNIEnv *env, jobject obj, jlong enclave_handler) {
    graal_isolate_t* isolate_t;
    graal_create_isolate_params_t p;
    graal_isolatethread_t* isolate_thread_t;

    int (*graal_create_isolate)(graal_create_isolate_params_t* params, graal_isolate_t** isolate, graal_isolatethread_t** thread);
    graal_create_isolate = (int (*)(graal_create_isolate_params_t*, graal_isolate_t**, graal_isolatethread_t**))
    dlsym((void *)enclave_handler, "graal_create_isolate");
    if (!graal_create_isolate) {
        fprintf(stderr, "dlsym error:%s\n", dlerror());
        return -1;
    }

    int ret = graal_create_isolate(NULL, &isolate_t, &isolate_thread_t);
    if (ret != 0) {
        fprintf(stderr, "graal_create_isolate create error:%s\n", dlerror());
        return ret;
    }

    // set isolate_t and isolate_thread_t back to MockInSvmEnclave.isolateHandle and MockInSvmEnclave.isolateThreadHandle
    jclass class_enclave = (*env)->GetObjectClass(env, obj);
    set_long_field_value(env, class_enclave, obj, "isolateHandle", (jlong)isolate_t);
    set_long_field_value(env, class_enclave, obj, "isolateThreadHandle", (jlong)isolate_thread_t);
    return ret;
}

JNIEXPORT jobject JNICALL JavaEnclave_MockSVMNativeLoadService(JNIEnv *env, jobject obj, jlong enclave_handler,
jlong isolate_handler, jbyteArray service_payload) {
    return service_operate_common(env, isolate_handler, service_payload, (java_enclave_stub) mock_in_svm_load_service_symbol);
}

JNIEXPORT jobject JNICALL JavaEnclave_MockSVMNativeInvokeMethod(JNIEnv *env, jobject obj, jlong enclave_handler,
jlong isolate_handler, jbyteArray invoke_wrapper_payload) {
    return service_operate_common(env, isolate_handler, invoke_wrapper_payload, (java_enclave_stub) mock_in_svm_invoke_service_symbol);
}

JNIEXPORT jobject JNICALL JavaEnclave_MockSVMNativeUnloadService(JNIEnv *env, jobject obj, jlong enclave_handler,
jlong isolate_handler, jbyteArray service_payload) {
    return service_operate_common(env, isolate_handler, service_payload, (java_enclave_stub) mock_in_svm_unload_service_symbol);
}

JNIEXPORT jint JNICALL JavaEnclave_MockSVMNativeSvmDetachIsolate(JNIEnv *env, jobject obj, jlong enclave_handler,
jlong isolate_thread_handler) {
    int (*graal_detach_all_threads_and_tear_down_isolate)(graal_isolatethread_t* isolateThread);
    graal_detach_all_threads_and_tear_down_isolate =
    (int (*)(graal_isolatethread_t*)) dlsym((void *)enclave_handler, "graal_detach_all_threads_and_tear_down_isolate");
    if (!graal_detach_all_threads_and_tear_down_isolate) {
        fprintf(stderr, "graal_detach_all_threads_and_tear_down_isolate error:%s\n", dlerror());
        return -1;
    }
    return (jint)graal_detach_all_threads_and_tear_down_isolate((graal_isolatethread_t*)isolate_thread_handler);
}

JNIEXPORT jint JNICALL JavaEnclave_MockSVMNativeDestroyEnclave(JNIEnv *env, jobject obj, jlong enclave_handler) {
    return dlclose((void *)enclave_handler);
}
