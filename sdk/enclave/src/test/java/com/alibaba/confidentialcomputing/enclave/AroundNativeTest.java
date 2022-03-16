package com.alibaba.confidentialcomputing.enclave;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * This class holds 2 main classes that are executed before and after maven surefire test by <a href="https://www.mojohaus.org/exec-maven-plugin/">exec-maven-plugin</a>.
 */
public class AroundNativeTest {
    public static final Path tmpTestNativeLibsDir = Paths.get("/tmp/javaenclavetest-native-libs");

    /**
     * Before test starts, create the temporary directory to hold the dynamic native libraries that will be created
     * during test. But the directory must be created beforehand, so that the {@code export LD_LIBRARY_PATH} action
     * taken by surefire plugin can take effect.
     */
    public static class PreTest {
        public static void main(String[] args) throws IOException {
            if (Files.notExists(tmpTestNativeLibsDir)) {
                Files.createDirectories(tmpTestNativeLibsDir);
            }
        }
    }

    public static class PostTest {
        public static void main(String[] args) throws IOException {
            if (Files.exists(tmpTestNativeLibsDir)) {
                Files.walkFileTree(tmpTestNativeLibsDir, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return super.visitFile(file, attrs);
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return super.postVisitDirectory(dir, exc);
                    }
                });
            }
        }
    }
}
