#include "tee_sdk_enclave_t.h"
#include "tee_sdk_symbol.h"

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
void __xpg_strerror_r() {TRACE_SYMBOL_CALL(); ASSERT();}
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
void fscanf() {TRACE_SYMBOL_CALL(); ASSERT();}
void fstatvfs() {TRACE_SYMBOL_CALL(); ASSERT();}
void fstatvfs64() {TRACE_SYMBOL_CALL(); ASSERT();}
void getgrnam_r() {TRACE_SYMBOL_CALL(); ASSERT();}
void getmntent_r() {TRACE_SYMBOL_CALL(); ASSERT();}
void getpwnam_r() {TRACE_SYMBOL_CALL(); ASSERT();}
void inet_pton() {TRACE_SYMBOL_CALL(); ASSERT();}
void inflate() {TRACE_SYMBOL_CALL(); ASSERT();}
void inflateEnd() {TRACE_SYMBOL_CALL(); ASSERT();}
void inflateInit2_() {TRACE_SYMBOL_CALL(); ASSERT();}
void inflateReset() {TRACE_SYMBOL_CALL(); ASSERT();}
void inflateSetDictionary() {TRACE_SYMBOL_CALL(); ASSERT();}
void ioctl() {TRACE_SYMBOL_CALL(); ASSERT();}
void lchown() {TRACE_SYMBOL_CALL(); ASSERT();}
void mknod() {TRACE_SYMBOL_CALL(); ASSERT();}
void pipe() {TRACE_SYMBOL_CALL(); ASSERT();}
void pthread_kill() {TRACE_SYMBOL_CALL(); ASSERT();}
void sched_getaffinity() {TRACE_SYMBOL_CALL(); ASSERT();}
void sendfile() {TRACE_SYMBOL_CALL(); ASSERT();}
void sendfile64() {TRACE_SYMBOL_CALL(); ASSERT();}
void setmntent() {TRACE_SYMBOL_CALL(); ASSERT();}
void sigaction() {TRACE_SYMBOL_CALL(); ASSERT();}
void sigaddset() {TRACE_SYMBOL_CALL(); ASSERT();}
void sigemptyset() {TRACE_SYMBOL_CALL(); ASSERT();}
void sigprocmask() {TRACE_SYMBOL_CALL(); ASSERT();}
void statvfs() {TRACE_SYMBOL_CALL(); ASSERT();}
void statvfs64() {TRACE_SYMBOL_CALL(); ASSERT();}
void symlink() {TRACE_SYMBOL_CALL(); ASSERT();}
void timezone() {TRACE_SYMBOL_CALL(); ASSERT();}

char* strcat(char* dest, const char* source) {
    TRACE_SYMBOL_CALL();
	if (dest == NULL || source == NULL) { return dest; }
	char* p = dest;
	while (*p != '\0') { p++; }
	while (*source != '\0') { *p = *source; p++; source++; }
	*p = '\0';
	return dest;
}

char* strcpy(char* dest,const char* sourse) {
    TRACE_SYMBOL_CALL();
    if(dest==NULL || sourse==NULL) return NULL;
    char* res=dest;
    while((*dest++ = *sourse++)!='\0');
    return res;
}

char* stpcpy(char *dest, const char *src) {
    TRACE_SYMBOL_CALL();
    size_t len = strlen (src);
    return memcpy(dest, src, len + 1) + len;
}

size_t __getdelim(char **lineptr, size_t *n, int delim, FILE *stream) {
    TRACE_SYMBOL_CALL();
    return getdelim(lineptr, n, delim, stream);
}

unsigned long int pthread_self(void) {
    TRACE_SYMBOL_CALL();
    return (unsigned long int)get_thread_data();
}

int pthread_attr_init(pthread_attr *attr) {
    TRACE_SYMBOL_CALL();
    return 0;
}

int pthread_setname_np() {
    TRACE_SYMBOL_CALL();
    return 0;
}

int pthread_attr_setdetachstate(pthread_attr *attr, int detachstate) {
    TRACE_SYMBOL_CALL();
    return 0;
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

int pthread_cond_timedwait() {
    TRACE_SYMBOL_CALL();
    return 0;
}

int pthread_getattr_np() {
    TRACE_SYMBOL_CALL();
    return 0;
}

int pthread_attr_setstacksize() {
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