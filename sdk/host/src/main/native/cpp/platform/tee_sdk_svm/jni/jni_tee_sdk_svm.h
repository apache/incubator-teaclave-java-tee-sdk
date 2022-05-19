#include <jni.h>

#ifndef _Included_jni_tee_sdk_svm
#define _Included_jni_tee_sdk_svm

#define TEE_SDK_SVM_NATIVE_CALL_SIGNATURE   "(JJ[B)Lcom/alibaba/confidentialcomputing/host/InnerNativeInvocationResult;"
#define TEE_SDK_SVM_RETURN_OBJECT_SIGNATURE "com/alibaba/confidentialcomputing/host/InnerNativeInvocationResult"

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
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL JavaEnclave_TeeSDKSVMNativeSvmAttachIsolate(JNIEnv *, jobject, jlong);

/*
 * Class:     com_alibaba_confidentialcomputing_host_TeeSdkEnclave
 * Method:    nativeLoadService
 * Signature: (JJ[B)Lcom/alibaba/confidentialcomputing/host/InnerNativeInvocationResult;
 */
JNIEXPORT jobject JNICALL JavaEnclave_TeeSDKSVMNativeLoadService(JNIEnv *, jobject, jlong, jlong, jbyteArray);

/*
 * Class:     com_alibaba_confidentialcomputing_host_TeeSdkEnclave
 * Method:    nativeInvokeMethod
 * Signature: (JJ[B)Lcom/alibaba/confidentialcomputing/host/InnerNativeInvocationResult;
 */
JNIEXPORT jobject JNICALL JavaEnclave_TeeSDKSVMNativeInvokeMethod(JNIEnv *, jobject, jlong, jlong, jbyteArray);

/*
 * Class:     com_alibaba_confidentialcomputing_host_TeeSdkEnclave
 * Method:    nativeUnloadService
 * Signature: (JJ[B)Lcom/alibaba/confidentialcomputing/host/InnerNativeInvocationResult;
 */
JNIEXPORT jobject JNICALL JavaEnclave_TeeSDKSVMNativeUnloadService(JNIEnv *, jobject, jlong, jlong, jbyteArray);

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

#ifdef __cplusplus
}
#endif
#endif