/*
 * Copyright (c) 2021, 2021, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021, 2021, Alibaba Group Holding Limited. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.alibaba.confidentialcomputing.enclave.c;

import com.oracle.svm.core.c.ProjectHeaderFile;
import com.oracle.svm.core.c.libc.TemporaryBuildDirectoryProvider;
import com.oracle.svm.core.util.VMError;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.c.CContext;
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
}
