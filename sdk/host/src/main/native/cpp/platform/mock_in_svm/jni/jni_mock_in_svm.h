#include <jni.h>

#ifndef _Included_jni_mock_in_svm
#define _Included_jni_mock_in_svm

#define MOCK_IN_SVM_NATIVE_CALL_SIGNATURE   "(JJ[B)Lcom/alibaba/confidentialcomputing/host/InnerNativeInvocationResult;"
#define MOCK_IN_SVM_RETURN_OBJECT_SIGNATURE "com/alibaba/confidentialcomputing/host/InnerNativeInvocationResult"

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
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL JavaEnclave_MockSVMNativeSvmAttachIsolate(JNIEnv *, jobject, jlong);

/*
 * Class:     JavaEnclave_MockSVMNativeLoadService
 * Method:    nativeLoadService
 * Signature: (JJ[B)Lcom/alibaba/confidentialcomputing/host/InnerNativeInvocationResult;
 */
JNIEXPORT jobject JNICALL JavaEnclave_MockSVMNativeLoadService(JNIEnv *, jobject, jlong, jlong, jbyteArray);

/*
 * Class:     JavaEnclave_MockSVMNativeInvokeMethod
 * Method:    nativeInvokeMethod
 * Signature: (JJ[B)Lcom/alibaba/confidentialcomputing/host/InnerNativeInvocationResult;
 */
JNIEXPORT jobject JNICALL JavaEnclave_MockSVMNativeInvokeMethod(JNIEnv *, jobject, jlong, jlong, jbyteArray);

/*
 * Class:     JavaEnclave_MockSVMNativeUnloadService
 * Method:    nativeUnloadService
 * Signature: (JJ[B)Lcom/alibaba/confidentialcomputing/host/InnerNativeInvocationResult;
 */
JNIEXPORT jobject JNICALL JavaEnclave_MockSVMNativeUnloadService(JNIEnv *, jobject, jlong, jlong, jbyteArray);

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