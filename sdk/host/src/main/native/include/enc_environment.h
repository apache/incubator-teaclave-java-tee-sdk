#ifndef __ENC_ENVIRONMENT_H
#define __ENC_ENVIRONMENT_H

#if defined(__cplusplus)
extern "C" {
#endif

typedef struct enc_data_struct{
    //char array is used as byte array to store serialized data
    char* data;
    int data_len;
}enc_data_t;

typedef struct callback_functions_struct{
     /*
      * This method is invoked inside java_enclave_invoke method's exception catch
      * section, when the execution is aborted by exceptions. The caller side can
      * decide what to do with the exception.
      * Exception details are passed back with parameters.
     */
    void (*exception_handler)(char* err_msg, char* stack_trace, char* exception_name);

    char* (*memcpy_char_pointer)(char* src, int len);
}callbacks_t;

#if defined(__cplusplus)
}
#endif
#endif
