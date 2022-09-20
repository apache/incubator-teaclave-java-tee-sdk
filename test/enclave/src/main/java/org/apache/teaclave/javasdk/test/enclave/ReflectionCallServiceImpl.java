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

package org.apache.teaclave.javasdk.test.enclave;

import org.apache.teaclave.javasdk.test.common.ReflectionCallService;

import com.google.auto.service.AutoService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@AutoService(ReflectionCallService.class)
public class ReflectionCallServiceImpl implements ReflectionCallService {
    private static Method addMethod;
    private static Method subMethod;

    static {
        try {
            addMethod = Class.forName(Calculate.class.getName()).getMethod("add", int.class, int.class);
            subMethod = Class.forName(Calculate.class.getName()).getMethod("sub", int.class, int.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int add(int a, int b) {
        try {
            return (int) addMethod.invoke(null, a, b);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int sub(int a, int b) {
        try {
            return (int) subMethod.invoke(null, a, b);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return -1;
    }
}