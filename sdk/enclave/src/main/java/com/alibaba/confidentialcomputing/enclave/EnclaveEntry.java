package com.alibaba.confidentialcomputing.enclave;

import com.alibaba.confidentialcomputing.enclave.c.EnclaveEnvironment.CallBacks;
import com.alibaba.confidentialcomputing.enclave.c.EnclaveEnvironment.EncData;
import com.alibaba.confidentialcomputing.enclave.framework.LoadServiceInvoker;
import com.alibaba.confidentialcomputing.enclave.framework.ServiceMethodInvoker;
import com.alibaba.confidentialcomputing.enclave.framework.UnloadServiceInvoker;
import com.oracle.svm.core.IsolateArgumentParser;
import com.oracle.svm.core.SubstrateGCOptions;
import com.oracle.svm.core.SubstrateOptions;
import com.oracle.svm.core.annotate.Uninterruptible;
import com.oracle.svm.core.c.CGlobalData;
import com.oracle.svm.core.c.CGlobalDataFactory;
import com.oracle.svm.core.c.function.CEntryPointActions;
import com.oracle.svm.core.c.function.CEntryPointCreateIsolateParameters;
import com.oracle.svm.core.c.function.CEntryPointNativeFunctions;
import com.oracle.svm.core.c.function.CEntryPointOptions;
import com.oracle.svm.core.headers.LibC;
import org.graalvm.compiler.hotspot.replacements.Log;
import org.graalvm.nativeimage.CurrentIsolate;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.Isolate;
import org.graalvm.nativeimage.PinnedObject;
import org.graalvm.nativeimage.StackValue;
import org.graalvm.nativeimage.UnmanagedMemory;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.struct.SizeOf;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CCharPointerPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.word.UnsignedWord;
import org.graalvm.word.WordFactory;

/**
 * This class defines the entry points for native image (shared library) deployed in TEE enclave.
 */
public class EnclaveEntry {
    private static volatile CallBacks callBackMethods;

    public static CallBacks getCallBackMethods() {
        return callBackMethods;
    }

    @Uninterruptible(reason = "Thread state not set up yet.", calleeMustBe = false)
    @CEntryPointOptions(prologue = CEntryPointOptions.NoPrologue.class, epilogue = CEntryPointOptions.NoEpilogue.class)
    @CEntryPoint(name = "create_isolate_with_params")
    public static int createIsolateWithParams(int argc, CCharPointerPointer argv, CEntryPointNativeFunctions.IsolatePointer isolatePr, CEntryPointNativeFunctions.IsolateThreadPointer thread) {
        CEntryPointCreateIsolateParameters args = StackValue.get(CEntryPointCreateIsolateParameters.class);
        args.setVersion(4);
        args.setArgc(argc);
        args.setArgv(argv);
        args.setIgnoreUnrecognizedArguments(false);
        args.setExitWhenArgumentParsingFails(true);
        int result = CEntryPointActions.enterCreateIsolate(args);
        if (result != 0) {
            return result;
        } else {
            if (isolatePr.isNonNull()) {
                isolatePr.write(CurrentIsolate.getIsolate());
            }
            if (thread.isNonNull()) {
                thread.write(CurrentIsolate.getCurrentThread());
            }
            int Xmx = IsolateArgumentParser.getIntOptionValue(IsolateArgumentParser.getOptionIndex(SubstrateGCOptions.MaxHeapSize));
            SubstrateGCOptions.MaxHeapSize.update((long) Xmx);
            int Xms = IsolateArgumentParser.getIntOptionValue(IsolateArgumentParser.getOptionIndex(SubstrateGCOptions.MinHeapSize));
            SubstrateGCOptions.MaxHeapSize.update((long) Xms);
            int Xmn = IsolateArgumentParser.getIntOptionValue(IsolateArgumentParser.getOptionIndex(SubstrateGCOptions.MaxNewSize));
            SubstrateGCOptions.MaxHeapSize.update((long) Xmn);
            return CEntryPointActions.leave();
        }
    }

    @SuppressWarnings("unused")
    // Align with head define file enc_exported_symbol.h if it changes.
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
    // Align with head define file enc_exported_symbol.h if it changes.
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
    // Align with head define file enc_exported_symbol.h if it changes.
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
