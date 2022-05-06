#ifndef __ENC_EXPORTED_SYMBOL_H
#define __ENC_EXPORTED_SYMBOL_H

#include "graal_isolate.h"

#if defined(__cplusplus)
extern "C" {
#endif

int java_loadservice_invoke(graal_isolate_t* thread, enc_data_t* input, enc_data_t* result, callbacks_t* callBacks);

int java_enclave_invoke(graal_isolate_t* thread, enc_data_t* input, enc_data_t* result, callbacks_t* callBacks);

int java_unloadservice_invoke(graal_isolate_t* thread, enc_data_t* input, enc_data_t* result, callbacks_t* callBacks);

#if defined(__cplusplus)
}
#endif
#endif