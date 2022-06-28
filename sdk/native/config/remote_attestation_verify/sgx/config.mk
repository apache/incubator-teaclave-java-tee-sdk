CC = gcc
CXX = g++

TEE_SDK_PATH = /opt/teesdk/sgxsdk
UBUNTU_OS = $(shell if [ -d "/usr/lib/x86_64-linux-gnu" ]; then echo "yes"; else echo "no"; fi;)
ifeq ("$(UBUNTU_OS)", "yes")
    DCAP_LIB_PATH = /usr/lib/x86_64-linux-gnu
else
    DCAP_LIB_PATH = /usr/lib64
endif

RA_VERIFY_INCDIR = -I$(TEE_SDK_PATH)/include
RA_VERIFY_LDFLAGS = -L$(DCAP_LIB_PATH) -lsgx_dcap_quoteverify -lsgx_dcap_ql
