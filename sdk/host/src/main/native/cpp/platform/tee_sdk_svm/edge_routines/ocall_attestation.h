#ifndef _OCALL_ATTESTATION_H_
#define _OCALL_ATTESTATION_H_

#include <stdio.h>

#include "sgx_urts.h"
#include "sgx_report.h"
#include "sgx_dcap_ql_wrapper.h"
#include "sgx_pce.h"
#include "sgx_error.h"
#include "sgx_quote_3.h"

#if defined(__cplusplus)
extern "C"
{
#endif
    quote3_error_t ocall_get_target_info(sgx_target_info_t *qe_target_info);
#if defined(__cplusplus)
}
#endif

#endif /* !_OCALL_ATTESTATION_H_ */