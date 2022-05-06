# parse enable MOCK_IN_SVM platform, ${MOCK_IN_SVM} is from make.sh script.
BUILD_MOCK_IN_SVM ?= $(shell echo ${MOCK_IN_SVM})
# parse JavaEnclave SDK base dir path, ${base_dir} is from make.sh script.
BASE_DIR_PATH = $(shell echo ${base_dir})

# parse BIN path.
BIN = $(BASE_DIR_PATH)/src/main/native/bin
# parse CONFIG path.
CONFIG = $(BASE_DIR_PATH)/src/main/native/config
# parse CPP path.
CPP = $(BASE_DIR_PATH)/src/main/native/cpp
# parse INCLUDE path.
INCLUDE = $(BASE_DIR_PATH)/src/main/native/include