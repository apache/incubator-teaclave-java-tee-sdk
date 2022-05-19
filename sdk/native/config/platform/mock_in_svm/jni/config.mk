CC = gcc
CXX = g++

# define mock_in_svm jni.cpp compile option.
DB_LDFLAGS = -Wl,-z,noexecstack -lc -ldl -lpthread -std=c99