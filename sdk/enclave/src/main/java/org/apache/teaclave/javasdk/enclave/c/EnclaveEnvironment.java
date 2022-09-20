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

package org.apache.teaclave.javasdk.enclave.c;

import com.oracle.svm.core.c.ProjectHeaderFile;
import com.oracle.svm.core.c.libc.TemporaryBuildDirectoryProvider;
import com.oracle.svm.core.util.VMError;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.c.CContext;
import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.function.CFunction.Transition;
import org.graalvm.nativeimage.c.function.CFunctionPointer;
import org.graalvm.nativeimage.c.function.InvokeCFunctionPointer;
import org.graalvm.nativeimage.c.struct.CField;
import org.graalvm.nativeimage.c.struct.CStruct;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.VoidPointer;
import org.graalvm.word.PointerBase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@CContext(EnclaveEnvironment.EnclaveDirectives.class)
public class EnclaveEnvironment {

    static class EnclaveDirectives implements CContext.Directives {

        private static final String HEADER_FILE = "native/enc_environment.h";

        @Override
        public List<String> getHeaderFiles() {
            // Register additional resolver to resolve header file from jar file as resource stream
            ProjectHeaderFile.HeaderResolversRegistry.registerAdditionalResolver((projectName, headerFile) -> {
                try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                    InputStream headerInputStream = EnclaveDirectives.class.getClassLoader().getResourceAsStream(HEADER_FILE);
                    if (headerInputStream == null) {
                        VMError.shouldNotReachHere("Can't find " + HEADER_FILE + " from classpath as resource");
                    }
                    headerInputStream.transferTo(os);
                    Path headerPath = ImageSingletons.lookup(TemporaryBuildDirectoryProvider.class).getTemporaryBuildDirectory().resolve(HEADER_FILE);
                    Path nativeDir = headerPath.getParent();
                    if (Files.notExists(nativeDir)) {
                        Files.createDirectory(nativeDir);
                    }
                    File tmpHeaderFile = Files.createFile(headerPath).toFile();
                    try (FileOutputStream fos = new FileOutputStream(tmpHeaderFile)) {
                        fos.write(os.toByteArray());
                        return new ProjectHeaderFile.HeaderSearchResult(Optional.of("\"" + tmpHeaderFile.getAbsolutePath() + "\""), tmpHeaderFile.getAbsolutePath());
                    }
                } catch (IOException e) {
                    VMError.shouldNotReachHere(e);
                }
                return null;
            });
            return Collections.singletonList(ProjectHeaderFile.resolve("enclave", HEADER_FILE));
        }
    }

    @CStruct("enc_data_t")
    public interface EncData extends PointerBase {
        @CField("data_len")
        int getLen();

        @CField("data_len")
        void setLen(int len);

        @CField("data")
        CCharPointer getData();

        @CField("data")
        void setData(CCharPointer data);
    }

    @CStruct("callbacks_t")
    public interface CallBacks extends PointerBase {
        @CField("exception_handler")
        ExceptionHandleFunctionPointer getExceptionHandler();

        @CField("exception_handler")
        void setExceptionHandler(ExceptionHandleFunctionPointer functionPointer);

        @CField("memcpy_char_pointer")
        MemCpyCCharPointerFunctionPointer getMemCpyCCharPointerFunctionPointer();

        @CField("memcpy_char_pointer")
        void setMemCpyCCharPointerFunctionPointer(MemCpyCCharPointerFunctionPointer functionPointer);

        @CField("get_random_number")
        NativeGetRandomNumberFunctionPointer getRandomNumber();

        @CField("get_random_number")
        NativeGetRandomNumberFunctionPointer setRandomNumber();
    }

    public interface ExceptionHandleFunctionPointer extends CFunctionPointer {
        @InvokeCFunctionPointer
        void invoke(CCharPointer errorMsg, CCharPointer stackTrace, CCharPointer exception);
    }

    public interface MemCpyCCharPointerFunctionPointer extends CFunctionPointer {
        @InvokeCFunctionPointer
        CCharPointer invoke(CCharPointer source, int length);
    }

    /**
     * A function pointer points to the native function that returns a pseudorandom number.
     */
    public interface NativeGetRandomNumberFunctionPointer extends CFunctionPointer {
        @InvokeCFunctionPointer
        int invoke(VoidPointer data, long size);
    }

    @CFunction(value = "physical_page_size")
    public native static long getPhysicalPageSize();

    @CFunction(value = "physical_page_number")
    public native static long getPhysicalPageNumber();

    @CFunction(value = "virtual_page_size", transition = Transition.NO_TRANSITION)
    public native static long getVirtualPageSize();
}
