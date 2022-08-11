#include <jni.h>

#ifndef _Included_jni_mock_in_svm
#define _Included_jni_mock_in_svm

typedef struct {
    int        ret;
    jbyteArray result;
} enclave_calling_stub_result;

#define REMOTE_ATTESTATION_CLASS_NAME                "com/alibaba/confidentialcomputing/host/exception/RemoteAttestationException"
#define ENCLAVE_CREATING_EXCEPTION                   "com/alibaba/confidentialcomputing/host/exception/EnclaveCreatingException"
#define ENCLAVE_DESTROYING_EXCEPTION                 "com/alibaba/confidentialcomputing/host/exception/EnclaveDestroyingException"
#define ENCLAVE_SERVICE_LOADING_EXCEPTION            "com/alibaba/confidentialcomputing/host/exception/ServicesLoadingException"
#define ENCLAVE_SERVICE_UNLOADING_EXCEPTION          "com/alibaba/confidentialcomputing/host/exception/ServicesUnloadingException"
#define ENCLAVE_SERVICE_INVOKING_EXCEPTION           "com/alibaba/confidentialcomputing/host/exception/EnclaveMethodInvokingException"

#define MOCK_IN_SVM_NATIVE_CALL_SIGNATURE            "(JJ[B)[B"

#define THROW_EXCEPTION(env, exception, info)                                  \
{                                                                              \
    jclass ra_class = (*env)->FindClass(env, exception);                       \
    if (ra_class == NULL) {                                                    \
        fprintf(stderr, "JavaEnclave Error:  ");                               \
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
JNIEXPORT void JNICALL Java_com_alibaba_confidentialcomputing_host_MockInSvmEnclave_registerNatives(JNIEnv *env, jclass cls);

/*
 * Class:     JavaEnclave_MockSVMNativeCreateEnclave
 * Method:    nativeCreateEnclave
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL JavaEnclave_MockSVMNativeCreateEnclave(JNIEnv *, jobject, jstring);

/*
 * Class:     JavaEnclave_MockSVMNativeSvmAttachIsolate
 * Method:    nativeSvmAttachIsolate
 * Signature: (JLjava/lang/String;)I
 */
JNIEXPORT jint JNICALL JavaEnclave_MockSVMNativeSvmAttachIsolate(JNIEnv *, jobject, jlong, jstring);

/*
 * Class:     JavaEnclave_MockSVMNativeLoadService
 * Method:    nativeLoadService
 * Signature: (JJ[B)[B
 */
JNIEXPORT jbyteArray JNICALL JavaEnclave_MockSVMNativeLoadService(JNIEnv *, jobject, jlong, jlong, jbyteArray);

/*
 * Class:     JavaEnclave_MockSVMNativeInvokeMethod
 * Method:    nativeInvokeMethod
 * Signature: (JJ[B)[B
 */
JNIEXPORT jbyteArray JNICALL JavaEnclave_MockSVMNativeInvokeMethod(JNIEnv *, jobject, jlong, jlong, jbyteArray);

/*
 * Class:     JavaEnclave_MockSVMNativeUnloadService
 * Method:    nativeUnloadService
 * Signature: (JJ[B)[B
 */
JNIEXPORT jbyteArray JNICALL JavaEnclave_MockSVMNativeUnloadService(JNIEnv *, jobject, jlong, jlong, jbyteArray);

/*
 * Class:     JavaEnclave_MockSVMNativeSvmDetachIsolate
 * Method:    nativeSvmDetachIsolate
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL JavaEnclave_MockSVMNativeSvmDetachIsolate(JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     JavaEnclave_MockSVMNativeDestroyEnclave
 * Method:    nativeDestroyEnclave
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL JavaEnclave_MockSVMNativeDestroyEnclave(JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif