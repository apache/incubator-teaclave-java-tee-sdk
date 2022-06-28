package com.alibaba.confidentialcomputing.enclave.system;

import com.alibaba.confidentialcomputing.enclave.c.EnclaveEnvironment;
import com.oracle.svm.core.heap.PhysicalMemory;
import com.oracle.svm.core.util.VMError;
import org.graalvm.word.UnsignedWord;
import org.graalvm.word.WordFactory;


/**
 * Physical memory implementation for Enclave environment. The sysconf(_SC_PAGESIZE()) and
 * sysconf(_SC_PHYS_PAGES()) might be invalid in SGX based SDK environment, so we read them from
 * custom native methods
 */
public class EnclavePhysicalMemory extends PhysicalMemory {

    static class PhysicalMemorySupportImpl implements PhysicalMemorySupport {
        @Override
        public UnsignedWord size() {
            long numberOfPhysicalMemoryPages = EnclaveEnvironment.getPhysicalPageNumber();
            if (numberOfPhysicalMemoryPages < 0) {
                throw VMError.shouldNotReachHere("Physical memory size (number of pages) not available");
            }
            long sizeOfAPhysicalMemoryPage = EnclaveEnvironment.getPhysicalPageSize();
            if (sizeOfAPhysicalMemoryPage < 0) {
                throw VMError.shouldNotReachHere("Physical memory size ( page size ) not available");
            }
            return WordFactory.unsigned(numberOfPhysicalMemoryPages).multiply(WordFactory.unsigned(sizeOfAPhysicalMemoryPage));
        }

        public static Class<PhysicalMemorySupport> getPhysicalMemorySupportClass() {
            return PhysicalMemorySupport.class;
        }
    }
}
