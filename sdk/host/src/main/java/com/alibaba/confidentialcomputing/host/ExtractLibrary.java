package com.alibaba.confidentialcomputing.host;

import java.io.*;

/**
 * JavaEnclave building tool will put native .so files into a java .jar file,
 * ExtractLibrary will extracts tee sdk's jni .so and enclave signed .so into
 * a temp path from the jar file. it's very convenient for deployment.
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
}
