#include <stdint.h>
#include <stdio.h>
#include <assert.h>

#include "unistd.h"
#include "sgx_mmap.h"

// get memory physical page size in enclave.
long physical_page_size() {
    TRACE_SYMBOL_CALL();
    return getpagesize();
}

// get memory physical page number in enclave.
long physical_page_number() {
    TRACE_SYMBOL_CALL();
    return get_heap_size() / getpagesize();
}

// get memory virtual page size in enclave.
long virtual_page_size() {
    TRACE_SYMBOL_CALL();
    return getpagesize();
}

// mmap and munmap is only partially supported in tee sdk enclave, and mmap doesn't
// support memory space reserve, but support memory space allocation.
void* mmap(void *hint, int size, int prot, int flags) {
    TRACE_SYMBOL_CALL();
    void *ptr = 0;
    // flags == 0x4022, svm runtime expects to reserve a memory buffer with giving start address hint;
    // flags == 0x22 and hint == 0x0, svm runtime expects to reserve a memory buffer, the start address depends.
    // Both the two scene, JavaEnclave SDK view them as enclave memory allocation, while not memory space reserve.
    if ((flags == 0x4022) || (flags == 0x22 && hint == 0x0 && prot == 0x3)) {
        // fd mapping is not supported in enclave, so the last two parameters of
        // (int fd, off_t offset) must be (-1, 0);
        // parameter pro = 0x3 (0B0011) indicates allocated buffer could be read and written.
        // parameter flags = 0x21, because ts_mmap only support this kind of operation.
        ptr = _mmap(hint, size, 0x3, 0x21, -1, 0);
    } else if (flags == 0x32) {
        ptr = hint;
    } else {
        // printf("JavaEnclave Warning: unsupported mmap operation in tee sdk enclave: 0x%lx, ptr is: %p, size is: %d, prot is: 0x%x, flags is: 0x%x.\n", (uint64_t)hint, ptr, size, prot, flags);
        ASSERT();
    }
    // printf("JavaEnclave Warning: mmap operation in tee sdk enclave: 0x%lx, ptr is: %p, size is: %d, prot is: 0x%x, flags is: 0x%x.\n", (uint64_t)hint, ptr, size, prot, flags);
    return ptr;
}

int munmap(void *addr, int size) {
    TRACE_SYMBOL_CALL();
    return _munmap(addr, size);
}