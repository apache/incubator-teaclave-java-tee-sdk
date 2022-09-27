// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

#ifndef _OCALL_SVM_H_
#define _OCALL_SVM_H_

#include <sys/resource.h>
#include <sys/mman.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>

#if defined(__cplusplus)
extern "C"
{
#endif
    int ocall_getrlimit(int resource, void *rlim);
    int ocall_malloc(size_t size, void *ptr);
#if defined(__cplusplus)
}
#endif

#endif /* !_OCALL_SVM_H_ */