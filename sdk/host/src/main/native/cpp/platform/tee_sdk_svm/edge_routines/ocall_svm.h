#ifndef _OCALL_SVM_H_
#define _OCALL_SVM_H_

#include <sys/resource.h>
#include <sys/mman.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>

#if defined(__cplusplus)
extern "C"
{
#endif
    int ocall_getrlimit(int resource, void *rlim);
    int ocall_malloc(size_t size, void *ptr);
#if defined(__cplusplus)
}
#endif

#endif /* !_OCALL_SVM_H_ */