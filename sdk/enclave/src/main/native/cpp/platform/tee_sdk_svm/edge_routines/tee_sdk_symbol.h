#ifndef _TEE_SDK_SYMBOL_H
#define _TEE_SDK_SYMBOL_H

#include <stdint.h>
#include <stdio.h>
#include <assert.h>

extern int enable_trace_symbol_calling;

#define ENABLE_TRACE_SYSCALL
#if defined(ENABLE_TRACE_SYSCALL)
#define TRACE_SYMBOL_CALL()  if(enable_trace_symbol_calling == 0x1) printf("JavaEnclave Warning: %s is called in enclave svm.\n", __FUNCTION__);
#else
#define TRACE_SYMBOL_CALL()
#endif

//#define UNSUPPORTED_SYSCALL_SYMBOL_ASSERT
#if defined(UNSUPPORTED_SYSCALL_SYMBOL_ASSERT)
#define ASSERT()  assert(-1);
#else
#define ASSERT()
#endif

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
void __xpg_strerror_r();
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
void fputs();
void fscanf();
void fstatvfs();
void fstatvfs64();
void getgrnam_r();
void getmntent_r();
void getpwnam_r();
void inet_pton();
void inflate();
void inflateEnd();
void inflateInit2_();
void inflateReset();
void inflateSetDictionary();
void ioctl();
void lchown();
void mknod();
void pipe();
void pthread_kill();
void sched_getaffinity();
void sendfile();
void sendfile64();
void setmntent();
void sigaction();
void sigaddset();
void sigemptyset();
void sigprocmask();
void statvfs();
void statvfs64();
void symlink();
void timezone();

char* strcat(char *restrict dest, const char *restrict src);
char* strcpy(char* dest,const char* src);
char* stpcpy(char *dest, const char *src);

size_t __getdelim(char **lineptr, size_t *n, int delim, FILE *stream);

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
int pthread_attr_init(pthread_attr *attr);
int pthread_attr_setdetachstate(pthread_attr *attr, int detachstate);
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
int pthread_setname_np();
int pthread_condattr_setclock();
int pthread_cond_timedwait();
int pthread_attr_destroy();
int pthread_attr_setstacksize();

#endif /* end of _TEE_SDK_SYMBOL_H */
