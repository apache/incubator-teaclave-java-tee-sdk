#include "tee_sdk_enclave_t.h"
#include "tee_sdk_symbol.h"

#ifndef _SGX_MMAP_H_
#define _SGX_MMAP_H_

long physical_page_size();
long physical_page_number();
long virtual_page_size();
void* mmap(void *hint, int size, int prot, int flags);
int munmap(void *addr, int size);
extern void* _mmap(void *addr, size_t length, int prot, int flags, int fd, int offset);
extern int _munmap(void *addr, size_t len);
extern size_t get_heap_size(void);

#endif /* !_SGX_MMAP_H_ */