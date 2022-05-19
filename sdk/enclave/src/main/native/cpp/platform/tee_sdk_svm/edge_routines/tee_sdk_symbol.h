#ifndef _TEE_SDK_SYMBOL_H
#define _TEE_SDK_SYMBOL_H

#include <stdint.h>
#include <stdio.h>
#include <assert.h>

void __fxstat();
void __fxstat64();
void __isnan();
void __libc_current_sigrtmax();
void __libc_malloc();
void __lxstat();
void __lxstat64();
void __sched_cpucount();
void __strdup();
void __xmknod();
void __xstat();
void __xstat64();
void chmod();
void chown();
void crc32();
void deflate();
void deflateBound();
void deflateEnd();
void deflateInit2_();
void deflateSetHeader();
void dlopen();
void dlsym();
void endmntent();
void fchmod();
void fchown();
void fpathconf();
void fstatvfs();
void fstatvfs64();
void getgrnam_r();
void getmntent_r();
void getpwnam_r();
void inflate();
void inflateEnd();
void inflateInit2_();
void inflateReset();
void inflateSetDictionary();
void lchown();
void lstat();
void mknod();
void pathconf();
void pipe();
void pthread_attr_init();
void pthread_attr_setdetachstate();
void pthread_attr_setstacksize();
void pthread_kill();
void pthread_setname_np();
void readlink();
void realpath();
void sched_getaffinity();
void sendfile();
void sendfile64();
void setmntent();
void sigaddset();
void sigemptyset();
void sigprocmask();
void statvfs();
void statvfs64();
void symlink();
void utimes();

int posix_memalign(void **memptr, size_t alignment, size_t size);

unsigned long int pthread_self();

typedef struct _thread_data {
    uint64_t self_addr;
    uint64_t __reserved_0;
    uint64_t __stack_base_addr;
    uint64_t __stack_limit_addr;
    uint64_t __first_ssa_gpr;
} thread_data;

typedef struct _pthread_attr {
    uint64_t __private[7];
} pthread_attr;

#define SE_PAGE_SIZE        0x1000
#define ROUND_TO(x, align)  (((x) + ((align)-1)) & ~((align)-1))
#define ROUND_TO_PAGE(x)    ROUND_TO(x, SE_PAGE_SIZE)

thread_data* get_thread_data(void);
unsigned long int pthread_self(void);
int pthread_attr_getstack(const pthread_attr *a, void ** addr, uint64_t *size);
int pthread_attr_getguardsize(const pthread_attr *a, size_t *size);
int mprotect();

// Avoid memory allocation in enclave failed, so we restrict resources by getrlimit
// In enclave.
#define FD_MAX (unsigned long)(64)

typedef struct {
    unsigned long rlim_cur;
    unsigned long rlim_max;
} rlimit;

int getrlimit(int resource, rlimit* rlim);
int setrlimit();
int pthread_condattr_init();
int pthread_condattr_setclock();
int pthread_attr_destroy();

#endif /* end of _TEE_SDK_SYMBOL_H */