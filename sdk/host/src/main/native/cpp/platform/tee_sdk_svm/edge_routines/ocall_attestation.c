#include "ocall_attestation.h"

// ocall_get_target_info get target info from host.
quote3_error_t ocall_get_target_info(sgx_target_info_t *qe_target_info) {
    return sgx_qe_get_target_info(qe_target_info);
}