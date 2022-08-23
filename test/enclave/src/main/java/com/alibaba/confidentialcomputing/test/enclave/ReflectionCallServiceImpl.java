package com.alibaba.confidentialcomputing.test.enclave;

import com.alibaba.confidentialcomputing.test.common.ReflectionCallService;

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