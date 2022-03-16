package com.alibaba.confidentialcomputing.enclave;

import com.oracle.svm.core.annotate.Uninterruptible;
import com.oracle.svm.core.c.CGlobalData;
import com.oracle.svm.core.c.CGlobalDataFactory;
import com.oracle.svm.core.c.function.CEntryPointActions;
import com.oracle.svm.core.c.function.CEntryPointOptions;
import org.graalvm.nativeimage.Isolate;
import org.graalvm.nativeimage.c.type.CCharPointer;

public class EnclavePrologue implements CEntryPointOptions.Prologue {
    private static final CGlobalData<CCharPointer> errorMessage = CGlobalDataFactory.createCString("Failed to enter (or attach to) the global isolate in the current thread.");

    @Uninterruptible(reason = "prologue")
    static void enter(Isolate isolate) {

        int code = CEntryPointActions.enterAttachThread(isolate, true);
        if (code != 0) {
            CEntryPointActions.failFatally(code, errorMessage.get());
        }
    }
}
