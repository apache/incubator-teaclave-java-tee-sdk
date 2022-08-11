#include <linux/limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <dlfcn.h>
#include <occlum_pal_api.h>

#include "jni_occlum_enclave.h"

#define OCCLUM_CMD_ARGS_MAX_LENGTH    50
#define OCCLUM_HARDWARE_PAL_PATH      "/opt/occlum/build/lib/libocclum-pal.so"
#define OCCLUM_SIMULATE_PAL_PATH      "/opt/occlum/build/lib/libocclum-pal_sim.so"
#define OCCLUM_CMD_PATH               "/usr/lib/dragonwell11/jre/bin/java"
#define OCCLUM_JVM_CMD_CP             "-cp"
#define OCCLUM_JVM_CMD_JAR_PATH       "/usr/app/*"
#define OCCLUM_JVM_CMD_MAIN_CLASS     "com.alibaba.confidentialcomputing.enclave.agent.EnclaveAgent"

void set_long_field_value(JNIEnv *env, jclass class_mirror, jobject obj, const char *field_name, jlong value) {
    jfieldID field_id = (*env)->GetFieldID(env, class_mirror, field_name, "J");
    (*env)->SetLongField(env, obj, field_id, value);
}

jint parse_http_handler_thread_pool_size(JNIEnv *env, jobject config) {
    jclass config_class = (*env)->GetObjectClass(env, config);
    jmethodID get_thread_pool_id = (*env)->GetMethodID(env, config_class, "getEnclaveAgentThreadPoolSize", "()I");
    return (*env)->CallObjectMethod(env, config, get_thread_pool_id);
}

jstring parse_log_level(JNIEnv *env, jobject config) {
    jclass config_class = (*env)->GetObjectClass(env, config);
    jmethodID get_log_level_id = (*env)->GetMethodID(env, config_class, "getLogLevel", "()Ljava/lang/String;");
    return (*env)->CallObjectMethod(env, config, get_log_level_id);
}

jobjectArray parse_jvm_cmd_args(JNIEnv *env, jobject config) {
    jclass config_class = (*env)->GetObjectClass(env, config);
    jmethodID get_jvm_args_id = (*env)->GetMethodID(env, config_class, "getEnclaveJVMArgs", "()[Ljava/lang/String;");
    return (*env)->CallObjectMethod(env, config, get_jvm_args_id);
}

static JNINativeMethod tee_lib_os_methods[] = {
    {"nativeCreateEnclave",    ENCLAVE_CREATING_SIGNATURE,  (void *)&JavaEnclave_TeeLibOSNativeCreateEnclave},
    {"nativeDestroyEnclave",   "(J)I",                      (void *)&JavaEnclave_TeeLibOSNativeDestroyEnclave},
};

JNIEXPORT void JNICALL
Java_com_alibaba_confidentialcomputing_host_EmbeddedLibOSEnclave_registerNatives(JNIEnv *env, jclass cls) {
    (*env)->RegisterNatives(env, cls, tee_lib_os_methods, sizeof(tee_lib_os_methods)/sizeof(tee_lib_os_methods[0]));
}

JNIEXPORT jint JNICALL JavaEnclave_TeeLibOSNativeCreateEnclave(JNIEnv *env, jobject obj, jint debug, jint sim, jint portHost, jint portEnclave, jobject config, jstring path) {
    char* occlum_pal_path = OCCLUM_HARDWARE_PAL_PATH;
    if (sim == 1) {
        occlum_pal_path = OCCLUM_SIMULATE_PAL_PATH;
    }

    void *lib_occlum_pal_handle = dlopen(occlum_pal_path, RTLD_LOCAL | RTLD_LAZY);
    if (!lib_occlum_pal_handle) {
        THROW_EXCEPTION(env, ENCLAVE_CREATING_EXCEPTION, "create tee lib os enclave: dlopen occlum_pal_path.so failed.")
    }

    // set .so file handle back to java enclave object.
    jclass class_enclave = (*env)->GetObjectClass(env, obj);
    set_long_field_value(env, class_enclave, obj, "enclaveHandle", (jlong)lib_occlum_pal_handle);

    // lookup symbol occlum_pal_init in libocclum-pal.so
    int (*occlum_pal_init)(const struct occlum_pal_attr *attr);
    occlum_pal_init = (int (*)(const struct occlum_pal_attr *))dlsym((void *)lib_occlum_pal_handle, "occlum_pal_init");
    if (!occlum_pal_init) {
        THROW_EXCEPTION(env, ENCLAVE_CREATING_EXCEPTION, "create tee lib os enclave: dlsym symbol occlum_pal_init failed.")
    }

    /* lookup symbol occlum_pal_create_process in libocclum-pal.so */
    int (*occlum_pal_create_process)(struct occlum_pal_create_process_args *args);
    occlum_pal_create_process = (int (*)(struct occlum_pal_create_process_args *))dlsym((void *)lib_occlum_pal_handle, "occlum_pal_create_process");
    if (!occlum_pal_create_process) {
        THROW_EXCEPTION(env, ENCLAVE_CREATING_EXCEPTION, "create tee lib os enclave: dlsym symbol occlum_pal_create_process failed.")
    }

    // lookup symbol occlum_pal_exec in libocclum-pal.so
    int (*occlum_pal_exec)(struct occlum_pal_exec_args *args);
    occlum_pal_exec = (int (*)(struct occlum_pal_exec_args *))dlsym((void *)lib_occlum_pal_handle, "occlum_pal_exec");
    if (!occlum_pal_exec) {
        THROW_EXCEPTION(env, ENCLAVE_CREATING_EXCEPTION, "create tee lib os enclave: dlsym symbol occlum_pal_exec failed.")
    }

    // parse occlum enclave log level.
    jstring log_level = parse_log_level(env, config);
    const char *log_level_str = (*env)->GetStringUTFChars(env, log_level, 0);
    const char *path_str = (path == 0) ? 0 : (*env)->GetStringUTFChars(env, path, 0);
    occlum_pal_attr_t pal_attr = OCCLUM_PAL_ATTR_INITVAL;
    pal_attr.instance_dir = path_str;
    pal_attr.log_level = log_level_str;
    if (occlum_pal_init(&pal_attr) < 0) {
        (*env)->ReleaseStringUTFChars(env, path, path_str);
        (*env)->ReleaseStringUTFChars(env, log_level, log_level_str);
        THROW_EXCEPTION(env, ENCLAVE_CREATING_EXCEPTION, "create tee lib os enclave: occlum_pal_init failed.")
    }

    const char *cmd_path = OCCLUM_CMD_PATH;
    char *cmd_args[OCCLUM_CMD_ARGS_MAX_LENGTH] = {NULL};
    t_jvm_args jvm_args_record[OCCLUM_CMD_ARGS_MAX_LENGTH] = {NULL};

    // parse jvm args from user config file.
    cmd_args[0] = cmd_path;
    jobjectArray jvm_args = parse_jvm_cmd_args(env, config);
    jsize length = (*env)->GetArrayLength(env, jvm_args);
    if (length >= OCCLUM_CMD_ARGS_MAX_LENGTH) {
        (*env)->ReleaseStringUTFChars(env, path, path_str);
        (*env)->ReleaseStringUTFChars(env, log_level, log_level_str);
        THROW_EXCEPTION(env, ENCLAVE_CREATING_EXCEPTION, "create tee lib os enclave: jvm args number exceeds max limitation 50.")
    }

    // parse jvm args and cache them in jvm_args_record for later release.
    int index = 0x0;
    for (; index < length; index++) {
        jvm_args_record[index].handler = (jstring)(*env)->GetObjectArrayElement(env, jvm_args, index);
        jvm_args_record[index].handler_str = (char *)(*env)->GetStringUTFChars(env, jvm_args_record[index].handler, 0);
        cmd_args[1+index] = jvm_args_record[index].handler_str;
    }
    // add cp path, main class name in cmd_args's tail.
    cmd_args[1+index++] = OCCLUM_JVM_CMD_CP;
    cmd_args[1+index++] = OCCLUM_JVM_CMD_JAR_PATH;
    cmd_args[1+index++] = OCCLUM_JVM_CMD_MAIN_CLASS;

    // add portHost number as java args.
    char port_host_buf[10];
    sprintf(port_host_buf, "%d", portHost);
    cmd_args[1+index++] = port_host_buf;

    // add portEnclave number as java args.
    char port_enclave_buf[10];
    sprintf(port_enclave_buf, "%d", portEnclave);
    cmd_args[1+index++] = port_enclave_buf;

    // add http thread pool size as java args.
    char thread_pool_size[10];
    sprintf(thread_pool_size, "%d", parse_http_handler_thread_pool_size(env, config));
    cmd_args[1+index] = thread_pool_size;

    struct occlum_stdio_fds io_fds = {
        .stdin_fd = STDIN_FILENO,
        .stdout_fd = STDOUT_FILENO,
        .stderr_fd = STDERR_FILENO,
    };

    // Use Occlum PAL to create new process
    int libos_tid = 0;
    struct occlum_pal_create_process_args create_process_args = {
        .path = cmd_path,
        .argv = cmd_args,
        .env = NULL,
        .stdio = (const struct occlum_stdio_fds *) &io_fds,
        .pid = &libos_tid,
    };
    if (occlum_pal_create_process(&create_process_args) < 0) {
        (*env)->ReleaseStringUTFChars(env, path, path_str);
        (*env)->ReleaseStringUTFChars(env, log_level, log_level_str);
        for (int i = 0x0; i < length; i++) {
            (*env)->ReleaseStringUTFChars(env, jvm_args_record[i].handler, jvm_args_record[i].handler_str);
        }
        THROW_EXCEPTION(env, ENCLAVE_CREATING_EXCEPTION, "create tee lib os enclave: occlum_pal_create_process failed.")
    }

    // Use Occlum PAL to execute the cmd
    int exit_status = 0;
    struct occlum_pal_exec_args exec_args = {
        .pid = libos_tid,
        .exit_value = &exit_status,
    };
    // occlum_pal_exec will block until application run in occlum enclave exit.
    if (occlum_pal_exec(&exec_args) < 0) {
        (*env)->ReleaseStringUTFChars(env, path, path_str);
        (*env)->ReleaseStringUTFChars(env, log_level, log_level_str);
        for (int i = 0x0; i < length; i++) {
            (*env)->ReleaseStringUTFChars(env, jvm_args_record[i].handler, jvm_args_record[i].handler_str);
        }
        THROW_EXCEPTION(env, ENCLAVE_CREATING_EXCEPTION, "create tee lib os enclave: occlum_pal_exec failed.")
    }

    (*env)->ReleaseStringUTFChars(env, path, path_str);
    (*env)->ReleaseStringUTFChars(env, log_level, log_level_str);
    for (int i = 0x0; i < length; i++) {
        (*env)->ReleaseStringUTFChars(env, jvm_args_record[i].handler, jvm_args_record[i].handler_str);
    }
    return 0;
}

JNIEXPORT jint JNICALL JavaEnclave_TeeLibOSNativeDestroyEnclave(JNIEnv *env, jobject obj, jlong handler) {
    // lookup symbol occlum_pal_destroy in libocclum-pal.so
    int (*occlum_pal_destroy)(void);
    occlum_pal_destroy = (int (*)(void))dlsym((void *)handler, "occlum_pal_destroy");

    if (!occlum_pal_destroy) {
        THROW_EXCEPTION(env, ENCLAVE_DESTROYING_EXCEPTION, "destroy tee lib os enclave: dlsym symbol occlum_pal_destroy failed.")
    }

    if (occlum_pal_destroy() != 0x0) {
        THROW_EXCEPTION(env, ENCLAVE_DESTROYING_EXCEPTION, "destroy tee lib os enclave: occlum_pal_destroy failed.")
    }

    if (dlclose((void *)handler) != 0x0) {
        THROW_EXCEPTION(env, ENCLAVE_DESTROYING_EXCEPTION, "destroy tee lib os enclave: close occlum_pal_path.so failed.")
    }
}