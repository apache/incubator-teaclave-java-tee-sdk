package com.alibaba.confidentialcomputing.enclave.substitutes;

import com.alibaba.confidentialcomputing.enclave.EnclaveEntry;
import com.alibaba.confidentialcomputing.enclave.EnclaveOptions;
import com.alibaba.confidentialcomputing.enclave.c.EnclaveEnvironment;
import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import com.oracle.svm.core.annotate.TargetElement;
import org.graalvm.nativeimage.UnmanagedMemory;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.nativeimage.c.type.VoidPointer;

import java.io.File;
import java.io.InputStream;
import java.util.function.BooleanSupplier;

/**
 * JDK reads random seed from two special files {@code /dev/random} and {@code /dev/urandom} on Linux platform. But they
 * are not necessarily existed in the enclave environment. So we need to substitute the methods that replies on them with
 * a native function that provides the same functionality.
 */
@SuppressWarnings("unused")
public final class NativePRNGSubstitutions {

    static class NoRandomFileInEnclave implements BooleanSupplier {

        @Override
        public boolean getAsBoolean() {
            return EnclaveOptions.RunInEnclave.getValue();
        }
    }

    @TargetClass(className = "sun.security.provider.NativePRNG", innerClass = "Variant", onlyWith = NoRandomFileInEnclave.class)
    static final class Target_sun_security_provider_NativePRNG_Variant {

    }

    @TargetClass(className = "sun.security.provider.NativePRNG", onlyWith = NoRandomFileInEnclave.class)
    static final class Target_sun_security_provider_NativePRNG {
        @Substitute
        private static Target_sun_security_provider_NativePRNG_RandomIO initIO(Target_sun_security_provider_NativePRNG_Variant v) {
            return new Target_sun_security_provider_NativePRNG_RandomIO(new File("/dev/random"), null);
        }
    }

    @TargetClass(className = "sun.security.provider.NativePRNG", innerClass = "RandomIO", onlyWith = NoRandomFileInEnclave.class)
    static final class Target_sun_security_provider_NativePRNG_RandomIO {
        @Alias
        File seedFile;
        @Alias
        byte[] nextBuffer;
        @Alias
        int bufferSize = 256;
        //Checkstyle: stop
        @Alias
        private Object LOCK_GET_BYTES;

        @Alias
        private Object LOCK_GET_SEED;

        @Alias
        private Object LOCK_SET_SEED;
        //Checkstyle: resume

        /**
         * The original {@code sun.security.provider.NativePRNG.RandomIO#RandomIO(File, File)}
         * method initializes field {@code sun.security.provider.NativePRNG.RandomIO#seedIn}
         * and field {@code sun.security.provider.NativePRNG.RandomIO#nextIn} to two special files {@code /dev/random}
         * and {@code /dev/urandom} respectively. However, these two files are not existed in Enclave
         * environment, leading to IOException at native image runtime. So we substitute the original method
         * to avoid creating InputStream from them. <p>
         * The {@code seedIn} and {@code nextIn} fields are only used as input parameter to call
         * {@code sun.security.provider.NativePRNG.RandomIO#readFully(InputStream, byte[])} method
         * to get random seeds. So we substitute it with {@link Target_sun_security_provider_NativePRNG_RandomIO#readFully(InputStream, byte[])}
         * to call the native method that can do the same functionality.
         *
         * @param seedFile /dev/random file, won't get InputStream from it now.
         * @param nextFile /dev/urandom file, will be ignored in the substitution method.
         */
        @Substitute
        @TargetElement(name = TargetElement.CONSTRUCTOR_NAME)
        Target_sun_security_provider_NativePRNG_RandomIO(File seedFile, File nextFile) {
            LOCK_GET_BYTES = new Object();
            LOCK_GET_SEED = new Object();
            LOCK_SET_SEED = new Object();
            this.seedFile = seedFile;
            nextBuffer = new byte[bufferSize];
        }

        @Substitute
        private static void readFully(InputStream in, byte[] data) {
            int len = data.length;
            EnclaveEnvironment.NativeGetRandomNumberFunctionPointer nativeGetRandomNumberFunctionPointer = EnclaveEntry.getCallBackMethods().getRandomNumber();
            if (nativeGetRandomNumberFunctionPointer.isNonNull()) {
                CCharPointer bytes = UnmanagedMemory.malloc(len);
                int ret = nativeGetRandomNumberFunctionPointer.invoke((VoidPointer) bytes, len);
                if (ret == 0) {
                    CTypeConversion.asByteBuffer(bytes, len).get(data);
                    UnmanagedMemory.free(bytes);
                } else {
                    UnmanagedMemory.free(bytes);
                    throw new RuntimeException("Fail to call the native random method in Enclave. Error code:" + ret);
                }
            } else {
                throw new RuntimeException("Callback function to oe_random is not set.");
            }
        }
    }
}
