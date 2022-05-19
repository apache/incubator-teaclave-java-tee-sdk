#include "tee_sdk_enclave_t.h"
#include "tee_sdk_symbol.h"

//#define ENABLE_TRACE_SYSCALL
#if defined(ENABLE_TRACE_SYSCALL)
#define TRACE_SYMBOL_CALL()  printf("JavaEnclave Warning: %s is called in enclave svm.\n", __FUNCTION__);
#else
#define TRACE_SYMBOL_CALL()
#endif

//#define UNSUPPORTED_SYSCALL_SYMBOL_ASSERT
#if defined(UNSUPPORTED_SYSCALL_SYMBOL_ASSERT)
#define ASSERT()  assert(-1);
#else
#define ASSERT()
#endif

void __fxstat() {TRACE_SYMBOL_CALL(); ASSERT();}
void __fxstat64() {TRACE_SYMBOL_CALL(); ASSERT();}
void __isnan() {TRACE_SYMBOL_CALL(); ASSERT();}
void __libc_current_sigrtmax() {TRACE_SYMBOL_CALL(); ASSERT();}
void __libc_malloc() {TRACE_SYMBOL_CALL(); ASSERT();}
void __lxstat() {TRACE_SYMBOL_CALL(); ASSERT();}
void __lxstat64() {TRACE_SYMBOL_CALL(); ASSERT();}
void __sched_cpucount() {TRACE_SYMBOL_CALL(); ASSERT();}
void __strdup() {TRACE_SYMBOL_CALL(); ASSERT();}
void __xmknod() {TRACE_SYMBOL_CALL(); ASSERT();}
void __xstat() {TRACE_SYMBOL_CALL(); ASSERT();}
void __xstat64() {TRACE_SYMBOL_CALL(); ASSERT();}
void chmod() {TRACE_SYMBOL_CALL(); ASSERT();}
void chown() {TRACE_SYMBOL_CALL(); ASSERT();}
void crc32() {TRACE_SYMBOL_CALL(); ASSERT();}
void deflate() {TRACE_SYMBOL_CALL(); ASSERT();}
void deflateBound() {TRACE_SYMBOL_CALL(); ASSERT();}
void deflateEnd() {TRACE_SYMBOL_CALL(); ASSERT();}
void deflateInit2_() {TRACE_SYMBOL_CALL(); ASSERT();}
void deflateSetHeader() {TRACE_SYMBOL_CALL(); ASSERT();}
void dlopen() {TRACE_SYMBOL_CALL(); ASSERT();}
void dlsym() {TRACE_SYMBOL_CALL(); ASSERT();}
void endmntent() {TRACE_SYMBOL_CALL(); ASSERT();}
void fchmod() {TRACE_SYMBOL_CALL(); ASSERT();}
void fchown() {TRACE_SYMBOL_CALL(); ASSERT();}
void fpathconf() {TRACE_SYMBOL_CALL(); ASSERT();}
void fstatvfs() {TRACE_SYMBOL_CALL(); ASSERT();}
void fstatvfs64() {TRACE_SYMBOL_CALL(); ASSERT();}
void getgrnam_r() {TRACE_SYMBOL_CALL(); ASSERT();}
void getmntent_r() {TRACE_SYMBOL_CALL(); ASSERT();}
void getpwnam_r() {TRACE_SYMBOL_CALL(); ASSERT();}
void inflate() {TRACE_SYMBOL_CALL(); ASSERT();}
void inflateEnd() {TRACE_SYMBOL_CALL(); ASSERT();}
void inflateInit2_() {TRACE_SYMBOL_CALL(); ASSERT();}
void inflateReset() {TRACE_SYMBOL_CALL(); ASSERT();}
void inflateSetDictionary() {TRACE_SYMBOL_CALL(); ASSERT();}
void lchown() {TRACE_SYMBOL_CALL(); ASSERT();}
void lstat() {TRACE_SYMBOL_CALL(); ASSERT();}
void mknod() {TRACE_SYMBOL_CALL(); ASSERT();}
void pathconf() {TRACE_SYMBOL_CALL(); ASSERT();}
void pipe() {TRACE_SYMBOL_CALL(); ASSERT();}
void pthread_attr_init() {TRACE_SYMBOL_CALL(); ASSERT();}
void pthread_attr_setdetachstate() {TRACE_SYMBOL_CALL(); ASSERT();}
void pthread_kill() {TRACE_SYMBOL_CALL(); ASSERT();}
void pthread_setname_np() {TRACE_SYMBOL_CALL(); ASSERT();}
void readlink() {TRACE_SYMBOL_CALL(); ASSERT();}
void realpath() {TRACE_SYMBOL_CALL(); ASSERT();}
void sched_getaffinity() {TRACE_SYMBOL_CALL(); ASSERT();}
void sendfile() {TRACE_SYMBOL_CALL(); ASSERT();}
void sendfile64() {TRACE_SYMBOL_CALL(); ASSERT();}
void setmntent() {TRACE_SYMBOL_CALL(); ASSERT();}
void sigaddset() {TRACE_SYMBOL_CALL(); ASSERT();}
void sigemptyset() {TRACE_SYMBOL_CALL(); ASSERT();}
void sigprocmask() {TRACE_SYMBOL_CALL(); ASSERT();}
void statvfs() {TRACE_SYMBOL_CALL(); ASSERT();}
void statvfs64() {TRACE_SYMBOL_CALL(); ASSERT();}
void symlink() {TRACE_SYMBOL_CALL(); ASSERT();}
void utimes() {TRACE_SYMBOL_CALL(); ASSERT();}

int posix_memalign(void **memptr, size_t alignment, size_t size) {
    TRACE_SYMBOL_CALL();
    void* ptr = malloc(size);
    if (ptr == NULL) { return -1; }
    *memptr = ptr;
    return 0;
}

unsigned long int pthread_self(void) {
    TRACE_SYMBOL_CALL();
    return (unsigned long int)get_thread_data();
}

int pthread_attr_getstack(const pthread_attr *a, void ** addr, size_t *size) {
    TRACE_SYMBOL_CALL();
    thread_data *self = (thread_data *)get_thread_data();
    uint64_t stack_base_addr = self->__stack_base_addr;
    uint64_t stack_limit_addr = self->__stack_limit_addr;
    *size = (int)ROUND_TO_PAGE(stack_base_addr - stack_limit_addr);
    *addr = (void *)stack_limit_addr;
    return 0;
}

int pthread_attr_getguardsize(const pthread_attr *a, size_t *size) {
    TRACE_SYMBOL_CALL();
    *size = 1;
    return 0;
}

int getrlimit(int resource, rlimit* rlim) {
	TRACE_SYMBOL_CALL();
    int ret = 0;
    ocall_getrlimit(&ret, resource, (void*)rlim);
    return ret;
}

int mprotect() {
    TRACE_SYMBOL_CALL();
    return 0;
}

int pthread_condattr_init() {
    TRACE_SYMBOL_CALL();
    return 0;
}

int pthread_condattr_setclock() {
    TRACE_SYMBOL_CALL();
    return 0;
}

int pthread_getattr_np() {
    TRACE_SYMBOL_CALL();
    return 0;
}

int pthread_attr_destroy() {
    TRACE_SYMBOL_CALL();
    return 0;
}

int setrlimit() {
    TRACE_SYMBOL_CALL();
    return 0;
}