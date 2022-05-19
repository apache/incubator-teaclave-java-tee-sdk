#ifndef _TEE_SDK_WRAPPER_H_
#define _TEE_SDK_WRAPPER_H_

#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>

#if defined(__cplusplus)
extern "C"
{
#endif
    void ocall_malloc(int*, int, void*);
#if defined(__cplusplus)
}
#endif

#endif /* !_TEE_SDK_WRAPPER_H_ */