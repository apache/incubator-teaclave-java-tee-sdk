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

package org.apache.teaclave.javasdk.host;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * Teaclave-java-tee-sdk building tool will put native .so files or .tgz files into a java .jar file,
 * ExtractLibrary will extract tee sdk's jni .so and enclave signed .so into a temp path
 * from the jar file.
 * extractAndDeCompressTgz will extract embedded lib os enclave's compressed .tgz image and
 * decompress .tgz file into target temp path from the jar file.
 * it's very convenient for deployment.
 */
final class ExtractLibrary {
    /**
     * check file exist in the .jar or not.
     *
     * @param classLoader define the search scope for lib .so.
     * @param file        lib.so's name in the jar file.
     * @return exist or not.
     */
    public static boolean isFileExist(ClassLoader classLoader, String file) throws IOException {
        try (InputStream in = classLoader.getResourceAsStream(file)) {
            return in != null;
        }
    }

    /**
     * get the temp file's full path.
     *
     * @param classLoader define the search scope for lib .so.
     * @param name        lib.so's name in the jar file.
     * @return the temp file's full path.
     */
    public static String extractLibrary(ClassLoader classLoader, String name) throws IOException {
        int pos = name.lastIndexOf('.');
        File file = File.createTempFile(name.substring(0, pos), name.substring(pos));
        String fullPath = file.getAbsolutePath();
        try (InputStream in = classLoader.getResourceAsStream(name);
             OutputStream out = new FileOutputStream(file)) {
            byte[] buf = new byte[4096];
            int length;
            while ((length = in.read(buf)) > 0) {
                out.write(buf, 0, length);
            }
        } finally {
            file.deleteOnExit();
        }
        return fullPath;
    }

    /**
     * get the temp file's full path.
     *
     * @param classLoader define the search scope for compressed fie .tgz.
     * @param name        lib.tgz name in the jar file.
     * @return the temp decompression file's full path.
     */
    public static String extractAndDeCompressTgz(ClassLoader classLoader, String name) throws IOException {
        String fullPath = extractLibrary(classLoader, name);
        String destDir = fullPath.replace(".tgz", "");
        deCompressTgz(fullPath, destDir);
        return destDir;
    }

    private static void deCompressTgz(String fullPath, String destDir) throws IOException {
        TarArchiveEntry entry;
        TarArchiveEntry[] subEntries;
        File subEntryFile = null;
        try (FileInputStream fis = new FileInputStream(fullPath);
             GZIPInputStream gis = new GZIPInputStream(fis);
             TarArchiveInputStream tis = new TarArchiveInputStream(gis)) {
            while ((entry = tis.getNextTarEntry()) != null) {
                StringBuilder entryFileName = new StringBuilder();
                entryFileName.append(destDir).append(File.separator).append(entry.getName());
                File entryFile = new File(entryFileName.toString());
                if (entry.isDirectory()) {
                    if (!entryFile.exists()) {
                        entryFile.mkdir();
                    }
                    subEntries = entry.getDirectoryEntries();
                    for (TarArchiveEntry subEntry : subEntries) {
                        try (OutputStream out = new FileOutputStream(subEntryFile)) {
                            subEntryFile = new File(entryFileName + File.separator + subEntry.getName());
                            IOUtils.copy(tis, out);
                        }
                    }
                } else {
                    checkFileExists(entryFile);
                    OutputStream out = new FileOutputStream(entryFile);
                    IOUtils.copy(tis, out);
                    out.close();
                }
            }
        }
    }

    private static void checkFileExists(File file) throws IOException {
        if (file.isDirectory()) {
            if (!file.exists()) {
                file.mkdir();
            }
        } else {
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
        }
    }
}
