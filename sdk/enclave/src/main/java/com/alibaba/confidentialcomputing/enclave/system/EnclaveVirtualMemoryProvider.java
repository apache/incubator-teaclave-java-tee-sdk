package com.alibaba.confidentialcomputing.enclave.system;

import com.alibaba.confidentialcomputing.enclave.c.EnclaveEnvironment;
import com.oracle.svm.core.annotate.Uninterruptible;
import com.oracle.svm.core.c.CGlobalData;
import com.oracle.svm.core.c.CGlobalDataFactory;
import com.oracle.svm.core.posix.PosixVirtualMemoryProvider;
import com.oracle.svm.core.util.VMError;
import org.graalvm.compiler.word.Word;
import org.graalvm.nativeimage.c.type.WordPointer;
import org.graalvm.word.UnsignedWord;
import org.graalvm.word.WordFactory;

/**
 * Virtual memory implementation for Enclave environment. The {@code sysconf(_SC_PAGE_SIZE())} might be
 * invalid in TEE and OE SDK environment, so we read it from a custom native method.
 */
public class EnclaveVirtualMemoryProvider extends PosixVirtualMemoryProvider {
    private static final CGlobalData<WordPointer> CACHED_PAGE_SIZE = CGlobalDataFactory.createWord();

    @Uninterruptible(reason = "Called from uninterruptible code.", mayBeInlined = true)
    private static UnsignedWord getVPageSize() {
        Word value = CACHED_PAGE_SIZE.get().read();
        if (value.equal(WordFactory.zero())) {
            long queried = EnclaveEnvironment.getVirtualPageSize();
            if (queried == -1L) {
                throw VMError.shouldNotReachHere("Virtual memory page size (_SC_PAGE_SIZE) not available");
            }
            value = WordFactory.unsigned(queried);
            CACHED_PAGE_SIZE.get().write(value);
        }
        return value;
    }

    @Override
    @Uninterruptible(reason = "May be called from uninterruptible code.", mayBeInlined = true)
    public UnsignedWord getGranularity() {
        return getVPageSize();
    }
}
