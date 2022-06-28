package com.alibaba.confidentialcomputing.host;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * JavaEnclave building tool will put native .so files or .tgz files into a java .jar file,
 * ExtractLibrary will extract tee sdk's jni .so and enclave signed .so into a temp path
 * from the jar file.
 * extractAndDeCompressTgz will extract embedded lib os enclave's compressed .tgz image and
 * decompress .tgz file into target temp path from the jar file.
 * it's very convenient for deployment.
 */
public final class ExtractLibrary {
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
                    for (int i = 0; i < subEntries.length; i++) {
                        try (OutputStream out = new FileOutputStream(subEntryFile)) {
                            subEntryFile = new File(entryFileName + File.separator + subEntries[i].getName());
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
