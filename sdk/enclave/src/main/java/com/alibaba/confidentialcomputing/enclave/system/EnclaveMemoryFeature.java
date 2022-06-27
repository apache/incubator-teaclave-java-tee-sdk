package com.alibaba.confidentialcomputing.enclave.system;

import com.alibaba.confidentialcomputing.enclave.EnclaveOptions;
import com.alibaba.confidentialcomputing.enclave.EnclavePlatFormSettings;
import com.alibaba.confidentialcomputing.enclave.c.EnclaveEnvironment;
import com.alibaba.confidentialcomputing.enclave.system.EnclavePhysicalMemory.PhysicalMemorySupportImpl;
import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.core.os.VirtualMemoryProvider;
import com.oracle.svm.core.util.VMError;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.impl.RuntimeClassInitializationSupport;

import java.util.List;

/**
 * Native image queries the memory page size and heap pages number at runtime with {@code sysconf(_SC_PHYS_PAGES)} and
 * {@code sysconf(_SC_PAGESIZE)}, just as POSIX defined. However, such operations are not supported by some enclave SDKs,
 * such as OE and TEE.
 * We define three custom native methods to get the corresponding memory information: {@link EnclaveEnvironment#getPhysicalPageNumber()},
 * {@link EnclaveEnvironment#getPhysicalPageSize()} and {@link EnclaveEnvironment#getVirtualPageSize()}. They should be
 * implemented in native code and linked by out framework. See {@code test/resources/native/enc_invoke_entry_test.c} and
 * {@code com.alibaba.confidentialcomputing.enclave.NativeImageTest#compileJNILibrary()} for details.
 * <p>
 */
@AutomaticFeature
public class EnclaveMemoryFeature implements Feature {
    @Override
    public List<Class<? extends Feature>> getRequiredFeatures() {
        try {
            Class<? extends Feature> physicalMemClass = (Class<? extends Feature>) Class.forName("com.oracle.svm.core.posix.linux.LinuxPhysicalMemory$PhysicalMemoryFeature");
            return List.of(physicalMemClass);
        } catch (ClassNotFoundException e) {
            throw VMError.shouldNotReachHere(e);
        }
    }

    @Override
    public void afterRegistration(AfterRegistrationAccess access) {
        if (EnclaveOptions.RunInEnclave.getValue()) {
            RuntimeClassInitializationSupport rci = ImageSingletons.lookup(RuntimeClassInitializationSupport.class);
            rci.initializeAtBuildTime("com.alibaba.confidentialcomputing.enclave.system.EnclaveVirtualMemoryProvider", "Native Image classes are always initialized at build time");
            EnclavePlatFormSettings.replaceImageSingletonEntry(PhysicalMemorySupportImpl.getPhysicalMemorySupportClass(), new PhysicalMemorySupportImpl());
            ImageSingletons.add(VirtualMemoryProvider.class, new EnclaveVirtualMemoryProvider());
        }
    }
}
