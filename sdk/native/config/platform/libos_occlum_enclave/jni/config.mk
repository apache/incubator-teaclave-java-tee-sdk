CC := gcc
OCCLUM_PREFIX ?= /opt/occlum
SGX_MODE ?= HW

ifneq ($(SGX_MODE), HW)
	URTS_LIBRARY_NAME := sgx_urts_sim
	UAE_SERVICE_LIBRARY_NAME := sgx_uae_service_sim
	OCCLUM_PAL_LIB := occlum-pal_sim
else
	URTS_LIBRARY_NAME := sgx_urts
	UAE_SERVICE_LIBRARY_NAME := sgx_uae_service
	OCCLUM_PAL_LIB := occlum-pal
endif

C_FLAGS := -Wl,-z,noexecstack -g -c -Wno-unused-parameter -I$(OCCLUM_PREFIX)/include

LINK_FLAGS := -lpthread -L$(OCCLUM_PREFIX)/build/lib -L/opt/teesdk/sgxsdk/lib64 -l$(URTS_LIBRARY_NAME) -l$(UAE_SERVICE_LIBRARY_NAME) -lsgx_uprotected_fs