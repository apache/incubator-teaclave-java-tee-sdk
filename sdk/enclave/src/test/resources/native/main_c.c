#include <stdlib.h>
#include <string.h>
#include "enc_environment.h"
#ifdef MUSL
#include "libmusl_svmenclavesdk.h"
#else
#include "libsvm_enclave_sdk.h"
#endif

long physical_page_size(){
    return 4096;
}

long physical_page_number(){
    return 24576;
}

long virtual_page_size(){
    return 4096;
}

int main(int argc, char** argv){
    graal_isolatethread_t *thread = NULL;
    graal_isolate_t *isolate = NULL;
    int size = 2;
    char** parameters = (char **)malloc(size * sizeof(char*));
    parameters[0] = NULL;
    parameters[1] = "-Xmx100m";
    return create_isolate_with_params(size, parameters, &isolate, &thread);
}