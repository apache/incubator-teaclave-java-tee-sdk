#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/stat.h>

#include <graal_isolate.h>

#include "sgx_urts.h"
#include "sgx_report.h"
#include "sgx_dcap_ql_wrapper.h"
#include "sgx_pce.h"
#include "sgx_error.h"
#include "sgx_quote_3.h"

#define SGX_AESM_ADR                 "SGX_AESM_ADDR"
#define UBUNTU_LIB_PATH              "/usr/lib/x86_64-linux-gnu"
#define RHEL_LIB_PATH                "/usr/lib64"
#define UBUNTU_LIB_SGX_PCE_PATH      "/usr/lib/x86_64-linux-gnu/libsgx_pce.signed.so"
#define UBUNTU_LIB_SGX_QE3_PATH      "/usr/lib/x86_64-linux-gnu/libsgx_qe3.signed.so"
#define UBUNTU_LIB_DCAP_QUOTE_PROV   "/usr/lib/x86_64-linux-gnu/libdcap_quoteprov.so.1"
#define RHEL_LIB_SGX_PCE_PATH        "/usr/lib64/libsgx_pce.signed.so"
#define RHEL_LIB_SGX_QE3_PATH        "/usr/lib64/libsgx_qe3.signed.so"
#define RHEL_LIB_DCAP_QUOTE_PROV     "/usr/lib64/libdcap_quoteprov.so.1"

#ifndef _Included_jni_generate_attestation_report
#define _Included_jni_generate_attestation_report

#ifdef __cplusplus
extern "C" {
#endif

quote3_error_t load_qe_signed_package();
quote3_error_t unload_qe_signed_package();

#ifdef __cplusplus
}
#endif
#endif