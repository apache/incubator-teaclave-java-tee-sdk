#include "generate_attestation_report.h"

// use_aesm_qe_service determines sgx aesm remote attestation service or local qe service.
bool use_aesm_qe_service() {
    bool flag = false;
    // If environment "SGX_AESM_ADDR" is set, remote qe will be adapted.
    char *sgx_aesm_address = getenv(SGX_AESM_ADR);
    if(sgx_aesm_address) {
        flag = true;
    }
    return flag;
}

// load_qe_signed_package loads all .signed packages which qe will use.
quote3_error_t load_qe_signed_package() {
    quote3_error_t qe3_ret = SGX_QL_SUCCESS;
    if(!use_aesm_qe_service()) {
        // Set enclave load policy.
        qe3_ret = sgx_qe_set_enclave_load_policy(SGX_QL_PERSISTENT);
        if(SGX_QL_SUCCESS != qe3_ret) {
            // printf("Error in set enclave load policy: 0x%04x\n", qe3_ret);
            return qe3_ret;
        }

        // Check it is Ubuntu-like OS system or RedHat-like OS system.
        char* lib_sgx_pce_path = NULL;
        char* lib_sgx_qe3_path = NULL;
        char* lib_dcap_quote_prov = NULL;
        const char* folder_ubuntu = UBUNTU_LIB_PATH;
        const char* folder_rhel = RHEL_LIB_PATH;
        struct stat sb;
        if (stat(folder_ubuntu, &sb) == 0 && S_ISDIR(sb.st_mode)) {
            // Ubuntu-like OS system.
            lib_sgx_pce_path = UBUNTU_LIB_SGX_PCE_PATH;
            lib_sgx_qe3_path = UBUNTU_LIB_SGX_QE3_PATH;
            lib_dcap_quote_prov = UBUNTU_LIB_DCAP_QUOTE_PROV;
        } else if (stat(folder_rhel, &sb) == 0 && S_ISDIR(sb.st_mode)) {
            // RedHat-like OS system.
            lib_sgx_pce_path = RHEL_LIB_SGX_PCE_PATH;
            lib_sgx_qe3_path = RHEL_LIB_SGX_QE3_PATH;
            lib_dcap_quote_prov = RHEL_LIB_DCAP_QUOTE_PROV;
        } else {
            // printf("Unsupported OS Platform Type.\n");
            return SGX_QL_SERVICE_UNAVAILABLE;
        }

        if (SGX_QL_SUCCESS != (qe3_ret = sgx_ql_set_path(SGX_QL_PCE_PATH, lib_sgx_pce_path)) ||
            SGX_QL_SUCCESS != (qe3_ret = sgx_ql_set_path(SGX_QL_QE3_PATH, lib_sgx_qe3_path))) {
            // printf("Error in set PCE/QE3 directory.\n");
            return qe3_ret;
        }
        if (SGX_QL_SUCCESS != (qe3_ret = sgx_ql_set_path(SGX_QL_QPL_PATH, lib_dcap_quote_prov))) {
            // printf("Warning: Cannot set QPL directory, you may get ECDSA quote with `Encrypted PPID` cert type.\n");
            return qe3_ret;
        }
    }
    return qe3_ret;
}

// unload_qe_signed_package unloads .signed packages load_qe_signed_package loaded.
quote3_error_t unload_qe_signed_package() {
    quote3_error_t qe3_ret = SGX_QL_SUCCESS;
    if(!use_aesm_qe_service()) {
        if(SGX_QL_SUCCESS != (qe3_ret = sgx_qe_cleanup_by_policy())) {
            // printf("Error in cleanup enclave load policy: 0x%04x\n", qe3_ret);
        }
    }
    return qe3_ret;
}
