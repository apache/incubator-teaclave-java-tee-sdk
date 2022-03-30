package com.alibaba.confidentialcomputing.enclave;

import com.alibaba.confidentialcomputing.enclave.c.EnclaveEnvironment.CallBacks;
import com.alibaba.confidentialcomputing.enclave.c.EnclaveEnvironment.EncData;
import com.alibaba.confidentialcomputing.enclave.framework.LoadServiceInvoker;
import com.alibaba.confidentialcomputing.enclave.framework.ServiceMethodInvoker;
import com.alibaba.confidentialcomputing.enclave.framework.UnloadServiceInvoker;
import com.oracle.svm.core.c.function.CEntryPointOptions;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.Isolate;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CTypeConversion;

/**
 * This class defines the entry points for native image (shared library) deployed in TEE enclave.
 */
public class EnclaveEntry {
    private static volatile CallBacks callBackMethods;

    public static CallBacks getCallBackMethods() {
        return callBackMethods;
    }

    @SuppressWarnings("unused")
    @CEntryPoint(name = "java_loadservice_invoke")
    @CEntryPointOptions(prologue = EnclavePrologue.class)
    public static int loadService(Isolate isolate, EncData input, EncData result, CallBacks callBacks) {
        callBackMethods = callBacks;
        int retCode = 0;
        try {
            InvocationWrapper.invoke(input, result, callBacks, ImageSingletons.lookup(LoadServiceInvoker.class));
        } catch (Throwable t) {
            retCode = handleFrameworkException(t);
        }
        return retCode;
    }

    @SuppressWarnings("unused")
    @CEntryPoint(name = "java_unloadservice_invoke")
    @CEntryPointOptions(prologue = EnclavePrologue.class)
    public static int unloadService(Isolate isolate, EncData input, EncData result, CallBacks callBacks) {
        callBackMethods = callBacks;
        int retCode = 0;
        try {
            InvocationWrapper.invoke(input, result, callBacks, ImageSingletons.lookup(UnloadServiceInvoker.class));
        } catch (Throwable t) {
            retCode = handleFrameworkException(t);
        }
        return retCode;
    }

    @SuppressWarnings("unused")
    @CEntryPoint(name = "java_enclave_invoke")
    @CEntryPointOptions(prologue = EnclavePrologue.class)
    public static int javaEnclaveInvoke(Isolate isolate, EncData input, EncData result, CallBacks callBacks) {
        callBackMethods = callBacks;
        int retCode = 0;
        try {
            InvocationWrapper.invoke(input, result, callBacks, ImageSingletons.lookup(ServiceMethodInvoker.class));
        } catch (Throwable t) {
            retCode = handleFrameworkException(t);
        }
        return retCode;
    }

    private static int handleFrameworkException(Throwable t) {
        if (callBackMethods.isNonNull() && callBackMethods.getExceptionHandler().isNonNull()) {
            StringBuilder stacktraceSB = new StringBuilder();
            for (StackTraceElement se : t.getStackTrace()) {
                stacktraceSB.append(se.toString()).append("\n");
            }
            try (
                    CTypeConversion.CCharPointerHolder stacktrace = CTypeConversion.toCString(stacktraceSB.toString());
                    CTypeConversion.CCharPointerHolder errMsg = CTypeConversion.toCString(t.getMessage());
                    CTypeConversion.CCharPointerHolder exception = CTypeConversion.toCString(t.getClass().toString())) {
                callBackMethods.getExceptionHandler().invoke(errMsg.get(), stacktrace.get(), exception.get());
            }
        } else {
            t.printStackTrace();
        }
        return 1;
    }
}
