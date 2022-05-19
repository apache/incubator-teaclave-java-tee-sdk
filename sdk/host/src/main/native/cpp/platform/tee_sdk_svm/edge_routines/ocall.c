#include "ocall.h"

int ocall_getrlimit(int resource, void *rlim) {
    return getrlimit(resource, (struct rlimit *)rlim);
}

int ocall_malloc(size_t size, void *ptr) {
    void* memptr = malloc(size);
    if (memptr != NULL) {
        *((char **)ptr) = (char *)memptr;
        return 0;
    }
    return -1;
}