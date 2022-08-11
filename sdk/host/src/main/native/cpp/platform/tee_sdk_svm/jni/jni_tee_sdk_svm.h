#include <jni.h>

#include "generate_attestation_report.h"

#ifndef _Included_jni_tee_sdk_svm
#define _Included_jni_tee_sdk_svm

typedef void (*enclave_calling_stub)(jlong, int*, graal_isolate_t*, void*, size_t, void*, size_t*);

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

#define TEE_SDK_SVM_NATIVE_CALL_SIGNATURE            "(JJ[B)[B"
#define TEE_SDK_REMOTE_ATTESTATION_REPORT_SIGNATURE  "(J[B)Lcom/alibaba/confidentialcomputing/host/TeeSdkAttestationReport;"
#define TEE_SDK_REMOTE_ATTESTATION_REPORT_CLASS_NAME "com/alibaba/confidentialcomputing/host/TeeSdkAttestationReport"

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

JNIEXPORT void JNICALL Java_com_alibaba_confidentialcomputing_host_TeeSdkEnclave_registerNatives(JNIEnv *env, jclass cls);

/*
 * Class:     com_alibaba_confidentialcomputing_host_TeeSdkEnclave
 * Method:    nativeCreateEnclave
 * Signature: (ILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL JavaEnclave_TeeSDKSVMNativeCreateEnclave(JNIEnv *, jobject, jint, jstring);

/*
 * Class:     com_alibaba_confidentialcomputing_host_TeeSdkEnclave
 * Method:    nativeSvmAttachIsolate
 * Signature: (JILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL JavaEnclave_TeeSDKSVMNativeSvmAttachIsolate(JNIEnv *, jobject, jlong, jint, jstring);

/*
 * Class:     com_alibaba_confidentialcomputing_host_TeeSdkEnclave
 * Method:    nativeLoadService
 * Signature: (JJ[B)[B
 */
JNIEXPORT jbyteArray JNICALL JavaEnclave_TeeSDKSVMNativeLoadService(JNIEnv *, jobject, jlong, jlong, jbyteArray);

/*
 * Class:     com_alibaba_confidentialcomputing_host_TeeSdkEnclave
 * Method:    nativeInvokeMethod
 * Signature: (JJ[B)[B
 */
JNIEXPORT jbyteArray JNICALL JavaEnclave_TeeSDKSVMNativeInvokeMethod(JNIEnv *, jobject, jlong, jlong, jbyteArray);

/*
 * Class:     com_alibaba_confidentialcomputing_host_TeeSdkEnclave
 * Method:    nativeUnloadService
 * Signature: (JJ[B)[B
 */
JNIEXPORT jbyteArray JNICALL JavaEnclave_TeeSDKSVMNativeUnloadService(JNIEnv *, jobject, jlong, jlong, jbyteArray);

/*
 * Class:     com_alibaba_confidentialcomputing_host_TeeSdkEnclave
 * Method:    nativeSvmDetachIsolate
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL JavaEnclave_TeeSDKSVMNativeSvmDetachIsolate(JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     com_alibaba_confidentialcomputing_host_TeeSdkEnclave
 * Method:    nativeDestroyEnclave
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL JavaEnclave_TeeSDKSVMNativeDestroyEnclave(JNIEnv *, jobject, jlong);

/*
 * Class:     com_alibaba_confidentialcomputing_host_TeeSdkEnclave
 * Method:    nativeGenerateAttestationReport
 * Signature: (J[B)Lcom/alibaba/confidentialcomputing/host/TeeSdkAttestationReport;
 */
JNIEXPORT jobject JNICALL JavaEnclave_TeeSDK_REMOTE_ATTESTATION_REPORT(JNIEnv *, jobject, jlong, jbyteArray);

#ifdef __cplusplus
}
#endif
#endif