#include "generate_attestation_report.h"

int generate_remote_attestation_report(void* hash, size_t hash_length, sgx_report_t* ra_report) {
	sgx_report_data_t report_data;
	quote3_error_t sgx_error;
	if (hash_length != SGX_REPORT_DATA_SIZE) {
	    return (int)SGX_ERROR_INVALID_PARAMETER;
	}
	memset(&report_data, 0, sizeof(sgx_report_data_t));
	memcpy(report_data.d, hash, SGX_REPORT_DATA_SIZE);

	sgx_target_info_t qe_target_info;
	memset(&qe_target_info, 0, sizeof(sgx_target_info_t));

	ocall_get_target_info(&sgx_error, &qe_target_info);
	if(sgx_error != SGX_QL_SUCCESS) {
	    return (int)sgx_error;
	}

	/* Generate the report for the app_enclave */
	return (int)sgx_create_report(&qe_target_info, &report_data, ra_report);
}
