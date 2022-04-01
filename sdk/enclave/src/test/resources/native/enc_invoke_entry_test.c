#include <stdlib.h>
#include <string.h>
#include "com_alibaba_confidentialcomputing_enclave_EnclaveTestHelper.h"
#include "enc_environment.h"
#ifdef MUSL
#include "libmusl_svmenclavesdk.h"
#else
#include "libsvm_enclave_sdk.h"
#endif

typedef int (*enclave_invoke)(graal_isolate_t* isolate, enc_data_t* input, enc_data_t* result, callbacks_t* callBacks);

long physical_page_size(){
#ifdef PAGE_SIZE
    return PAGE_SIZE;
#else
    return 4096;
#endif
}

long physical_page_number(){
#ifdef HEAP_PAGES
    return HEAP_PAGES;
#else
    return 24576;
#endif
}

long virtual_page_size(){
    return 4096;
}

 char* memcpy_char_pointer(char* src, int len){
    int size = sizeof(char);
    char *dest = (char*)malloc(len*size);
    memcpy(dest, src, len);
    return dest;
 }

/*
* Tested by RunWithNativeImageTest.testCallNativeGetRandomNumber.
*/
int get_native_random_number(void* data, long size){
    char* tm = (char*)data;
    for(int i=0;i<size;i++){
        tm[i] = i%256;
    }
    return 0;
}

static graal_isolatethread_t *thread = NULL;
static graal_isolate_t *isolate = NULL;

jbyteArray enclave_call(JNIEnv* env, jclass clazz, jbyteArray data, enclave_invoke invoke){
jboolean isCopy;
	jbyte* a = (*env)->GetByteArrayElements(env, data, &isCopy);
	enc_data_t invoke_data;
	invoke_data.data=(char*)a;
	invoke_data.data_len=(*env)->GetArrayLength(env, data);

    callbacks_t callback_methods;
    callback_methods.memcpy_char_pointer=&memcpy_char_pointer;
    callback_methods.get_random_number=&get_native_random_number;
    callback_methods.exception_handler=NULL; // Must explicitly set

    enc_data_t ret;
    int exit_code = invoke(isolate, &invoke_data, &ret, &callback_methods);
    jbyteArray retVal;
    if(exit_code == 0 ){
        retVal = (*env)->NewByteArray(env, ret.data_len);
        jbyte *buf = (*env)->GetByteArrayElements(env, retVal, NULL);
        memcpy(buf, ret.data, ret.data_len);
        (*env)->ReleaseByteArrayElements(env, retVal, buf, 0);
        //printf("Returned type is %.*s\n", ret.verify_info_len, ret.verify_info);
        free(ret.data);
    }else{
        retVal = NULL;
    }
	return retVal;
}

JNIEXPORT void JNICALL Java_com_alibaba_confidentialcomputing_enclave_EnclaveTestHelper_createIsolate
   (JNIEnv *env, jclass clazz){
       if (graal_create_isolate(NULL, &isolate, &thread) != 0) {
         fprintf(stderr, "error on isolate creation or attach\n");
       }
}

JNIEXPORT void JNICALL Java_com_alibaba_confidentialcomputing_enclave_EnclaveTestHelper_destroyIsolate
     (JNIEnv *env, jclass clazz){
     //graal_tear_down_isolate(thread);
     graal_detach_all_threads_and_tear_down_isolate(thread);
}

JNIEXPORT jbyteArray JNICALL Java_com_alibaba_confidentialcomputing_enclave_EnclaveTestHelper_loadService
(JNIEnv* env, jclass clazz, jbyteArray data) {
    return enclave_call(env, clazz, data, java_loadservice_invoke);
}

JNIEXPORT jbyteArray JNICALL Java_com_alibaba_confidentialcomputing_enclave_EnclaveTestHelper_unloadService
  (JNIEnv* env, jclass clazz, jbyteArray data){
  return enclave_call(env, clazz, data, java_unloadservice_invoke);
}

JNIEXPORT jbyteArray JNICALL Java_com_alibaba_confidentialcomputing_enclave_EnclaveTestHelper_invokeEnclave
(JNIEnv* env, jclass clazz, jbyteArray data) {
    return enclave_call(env, clazz, data, java_enclave_invoke);
}
