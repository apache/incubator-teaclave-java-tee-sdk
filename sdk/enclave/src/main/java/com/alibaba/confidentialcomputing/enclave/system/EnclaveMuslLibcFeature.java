package com.alibaba.confidentialcomputing.enclave.system;

import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.core.c.libc.LibCBase;
import com.oracle.svm.core.posix.linux.libc.LibCFeature;
import com.oracle.svm.core.posix.linux.libc.MuslLibC;
import com.oracle.svm.core.util.UserError;
import org.graalvm.compiler.serviceprovider.JavaVersionUtil;
import org.graalvm.nativeimage.ImageSingletons;

import java.util.ServiceLoader;

@AutomaticFeature
public class EnclaveMuslLibcFeature extends LibCFeature {

    @Override
    public void afterRegistration(AfterRegistrationAccess access) {
        String targetLibC = LibCOptions.UseLibC.getValue();
        ServiceLoader<LibCBase> loader = ServiceLoader.load(LibCBase.class);
        for (LibCBase libc : loader) {
            if (libc.getName().equals(targetLibC)) {
                if (libc.getName().equals(MuslLibC.NAME)) {
                    if (JavaVersionUtil.JAVA_SPEC < 11) {
                        throw UserError.abort("Musl can only be used with labsjdk 11+.");
                    }
                } else {
                    libc.checkIfLibCSupported();
                }
                ImageSingletons.add(LibCBase.class, libc);
                return;
            }
        }
        throw UserError.abort("Unknown libc %s selected. Please use one of the available libc implementations.", targetLibC);
    }
}
