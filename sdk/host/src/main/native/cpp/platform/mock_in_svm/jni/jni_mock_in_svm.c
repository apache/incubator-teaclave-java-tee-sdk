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

typedef int (*mock_enclave_stub)(graal_isolate_t*, enc_data_t*, enc_data_t*, callbacks_t*);

static JNINativeMethod mock_in_svm_methods[] = {
    {"nativeCreateEnclave",       "(Ljava/lang/String;)I",           (void *)&JavaEnclave_MockSVMNativeCreateEnclave},
    {"nativeSvmAttachIsolate",    "(JLjava/lang/String;)I",          (void *)&JavaEnclave_MockSVMNativeSvmAttachIsolate},
    {"nativeLoadService",         "(JJ[B)[B",                        (void *)&JavaEnclave_MockSVMNativeLoadService},
    {"nativeInvokeMethod",        "(JJ[B)[B",                        (void *)&JavaEnclave_MockSVMNativeInvokeMethod},
    {"nativeUnloadService",       "(JJ[B)[B",                        (void *)&JavaEnclave_MockSVMNativeUnloadService},
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
    if (ptr == NULL) { return NULL; }
    memcpy(ptr, src, len);
    return (char*)ptr;
}

enclave_calling_stub_result mock_enclave_calling_entry(JNIEnv *env, jlong isolate_handler, jbyteArray payload, mock_enclave_stub stub) {
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

    callbacks_t callback_methods;
    callback_methods.memcpy_char_pointer = &memcpy_char_pointer;
    callback_methods.exception_handler = NULL;
    callback_methods.get_random_number = NULL;

    result_wrapper.ret = stub((graal_isolate_t*)isolate_handler, &input, &output, &callback_methods);
    if (result_wrapper.ret != 0) {
        (*env)->ReleaseByteArrayElements(env, payload, payload_copy, 0);
        free(output.data);
        return result_wrapper;
    }

    // create a byte array.
    invocation_result_array = (*env)->NewByteArray(env, output.data_len);
    jbyte *invocation_result_array_ptr = (*env)->GetByteArrayElements(env, invocation_result_array, NULL);
    memcpy(invocation_result_array_ptr, output.data, output.data_len);

    (*env)->ReleaseByteArrayElements(env, payload, payload_copy, 0);
    // free buffer malloc in jni.
    (*env)->ReleaseByteArrayElements(env, invocation_result_array, invocation_result_array_ptr, 0);
    // free buffer malloc in native image by callback mechanism.
    free(output.data);

    result_wrapper.result = invocation_result_array;
    return result_wrapper;
}

JNIEXPORT jint JNICALL
JavaEnclave_MockSVMNativeCreateEnclave(JNIEnv *env, jobject obj, jstring path) {
    const char *path_str = (path == 0) ? 0 : (*env)->GetStringUTFChars(env, path, 0);
    void *enclave_handler = dlopen(path_str , RTLD_LOCAL | RTLD_LAZY);
    (*env)->ReleaseStringUTFChars(env, path, path_str);
    if (enclave_handler == 0x0) {
        THROW_EXCEPTION(env, ENCLAVE_CREATING_EXCEPTION, "mock in svm dlopen error.")
    }
    // find load service symbol.
    mock_in_svm_load_service_symbol = dlsym((void *)enclave_handler, "java_loadservice_invoke");
    if (!mock_in_svm_load_service_symbol) {
        dlclose(enclave_handler);
        THROW_EXCEPTION(env, ENCLAVE_CREATING_EXCEPTION, "java_loadservice_invoke error.")
    }
    // find invoke service symbol.
    mock_in_svm_invoke_service_symbol = dlsym((void *)enclave_handler, "java_enclave_invoke");
    if (!mock_in_svm_invoke_service_symbol) {
        dlclose(enclave_handler);
        THROW_EXCEPTION(env, ENCLAVE_CREATING_EXCEPTION, "mock_in_svm_invoke_service_symbol error.")
    }
    // find unload service symbol.
    mock_in_svm_unload_service_symbol = dlsym((void *)enclave_handler, "java_unloadservice_invoke");
    if (!mock_in_svm_unload_service_symbol) {
        dlclose(enclave_handler);
        THROW_EXCEPTION(env, ENCLAVE_CREATING_EXCEPTION, "mock_in_svm_unload_service_symbol error.")
    }
    // set enclave_handler back to MockInSvmEnclave.enclaveSvmSdkHandle field.
    jclass class_enclave = (*env)->GetObjectClass(env, obj);
    set_long_field_value(env, class_enclave, obj, "enclaveSvmSdkHandle", (jlong)enclave_handler);
    return 0;
}

JNIEXPORT jint JNICALL
JavaEnclave_MockSVMNativeSvmAttachIsolate(JNIEnv *env, jobject obj, jlong enclave_handler, jstring args) {
    graal_isolate_t* isolate_t;
    graal_isolatethread_t* isolate_thread_t;

    int (*create_isolate_with_params)(int argc, char** parameters, graal_isolate_t** isolate, graal_isolatethread_t** thread);
    create_isolate_with_params = (int (*)(int, char**, graal_isolate_t**, graal_isolatethread_t**)) dlsym((void *)enclave_handler, "create_isolate_with_params");
    if (!create_isolate_with_params) {
        THROW_EXCEPTION(env, ENCLAVE_CREATING_EXCEPTION, "create isolate dlsym error.")
    }

    char *args_str = (*env)->GetStringUTFChars(env, args, 0);
    int argc = 2;
    char* parameters[2];
    parameters[0] = NULL;
    parameters[1] = args_str;

    if (create_isolate_with_params(argc, parameters, &isolate_t, &isolate_thread_t) != 0) {
        (*env)->ReleaseStringUTFChars(env, args, args_str);
        THROW_EXCEPTION(env, ENCLAVE_CREATING_EXCEPTION, "graal_create_isolate create error.")
    }

    (*env)->ReleaseStringUTFChars(env, args, args_str);
    // set isolate_t and isolate_thread_t back to MockInSvmEnclave.isolateHandle and MockInSvmEnclave.isolateThreadHandle
    jclass class_enclave = (*env)->GetObjectClass(env, obj);
    set_long_field_value(env, class_enclave, obj, "isolateHandle", (jlong)isolate_t);
    set_long_field_value(env, class_enclave, obj, "isolateThreadHandle", (jlong)isolate_thread_t);
    return 0;
}

JNIEXPORT jbyteArray JNICALL
JavaEnclave_MockSVMNativeLoadService(JNIEnv *env, jobject obj, jlong enclave_handler, jlong isolate_handler, jbyteArray load_service_payload) {
    enclave_calling_stub_result result_wrapper =  mock_enclave_calling_entry(env, isolate_handler, load_service_payload, (mock_enclave_stub) mock_in_svm_load_service_symbol);
    if (result_wrapper.ret != 0) {
        THROW_EXCEPTION(env, ENCLAVE_SERVICE_LOADING_EXCEPTION, "tee sdk service loading native call failed.")
    }
    return result_wrapper.result;
}

JNIEXPORT jbyteArray JNICALL
JavaEnclave_MockSVMNativeInvokeMethod(JNIEnv *env, jobject obj, jlong enclave_handler, jlong isolate_handler, jbyteArray invoke_payload) {
    enclave_calling_stub_result result_wrapper = mock_enclave_calling_entry(env, isolate_handler, invoke_payload, (mock_enclave_stub) mock_in_svm_invoke_service_symbol);
    if (result_wrapper.ret != 0) {
        THROW_EXCEPTION(env, ENCLAVE_SERVICE_INVOKING_EXCEPTION, "tee sdk service method invoking native call failed.")
    }
    return result_wrapper.result;
}

JNIEXPORT jbyteArray JNICALL
JavaEnclave_MockSVMNativeUnloadService(JNIEnv *env, jobject obj, jlong enclave_handler, jlong isolate_handler, jbyteArray unload_service_payload) {
    enclave_calling_stub_result result_wrapper = mock_enclave_calling_entry(env, isolate_handler, unload_service_payload, (mock_enclave_stub) mock_in_svm_unload_service_symbol);
    if (result_wrapper.ret != 0) {
        THROW_EXCEPTION(env, ENCLAVE_SERVICE_UNLOADING_EXCEPTION, "tee sdk service unloading native call failed.")
    }
    return result_wrapper.result;
}

JNIEXPORT jint JNICALL
JavaEnclave_MockSVMNativeSvmDetachIsolate(JNIEnv *env, jobject obj, jlong enclave_handler, jlong isolate_thread_handler) {
    int (*graal_detach_all_threads_and_tear_down_isolate)(graal_isolatethread_t* isolateThread);
    graal_detach_all_threads_and_tear_down_isolate =
    (int (*)(graal_isolatethread_t*)) dlsym((void *)enclave_handler, "graal_detach_all_threads_and_tear_down_isolate");
    if (!graal_detach_all_threads_and_tear_down_isolate) {
        THROW_EXCEPTION(env, ENCLAVE_DESTROYING_EXCEPTION, "graal_detach_all_threads_and_tear_down_isolate dlsym error.")
    }
    if (0x0 != graal_detach_all_threads_and_tear_down_isolate((graal_isolatethread_t*)isolate_thread_handler)) {
        THROW_EXCEPTION(env, ENCLAVE_DESTROYING_EXCEPTION, "graal_detach_all_threads_and_tear_down_isolate error.")
    }
    return 0;
}

JNIEXPORT jint JNICALL
JavaEnclave_MockSVMNativeDestroyEnclave(JNIEnv *env, jobject obj, jlong enclave_handler) {
    if(0x0 != dlclose((void *)enclave_handler)) {
        THROW_EXCEPTION(env, ENCLAVE_DESTROYING_EXCEPTION, "dlclose failed.")
    }
    return 0;
}
