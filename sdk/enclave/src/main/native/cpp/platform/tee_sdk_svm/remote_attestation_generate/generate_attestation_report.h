#ifndef _GENERATE_ATTESTATION_REPORT_H_
#define _GENERATE_ATTESTATION_REPORT_H_

#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>

#include "sgx_trts.h"
#include "sgx_error.h"
#include "sgx_report.h"
#include "sgx_utils.h"
#include "sgx_quote_3.h"
#include "sgx_ql_lib_common.h"

#if defined(__cplusplus)
extern "C"
{
#endif
    int generate_remote_attestation_report(void* hash, size_t hash_length, sgx_report_t* ra_report);
#if defined(__cplusplus)
}
#endif

#endif /* !_GENERATE_ATTESTATION_REPORT_H_ */
