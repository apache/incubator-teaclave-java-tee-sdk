package com.alibaba.confidentialcomputing.enclave;

import com.oracle.svm.core.SubstrateOptions;
import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.core.c.libc.LibCBase;
import com.oracle.svm.core.c.libc.TemporaryBuildDirectoryProvider;
import com.oracle.svm.core.jdk.JNIRegistrationUtil;
import com.oracle.svm.core.util.VMError;
import com.oracle.svm.hosted.FeatureImpl;
import com.oracle.svm.hosted.ImageClassLoader;
import com.oracle.svm.hosted.NativeImageGenerator;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.Platform;
import org.graalvm.nativeimage.Platforms;
import org.graalvm.nativeimage.hosted.Feature;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static com.alibaba.confidentialcomputing.enclave.NativeCommandUtil.GRAALVM_HOME;

/**
 * THe original {@code libsunec.a} library provided by GraalVM (which is copied from OpenJDK) has C++ operations which
 * are not supported by some Enclave SDKs (e.g. OpenEnclave and Tee SDK), therefore, a substituted library that replies
 * only on C symbols is required.
 * <p>
 * This class compiles the new C only {@code libsunec.a} and outputs it into the path specified by {@link SubstrateOptions#Path}
 * options along with other SVM artifacts on demand.
 */
@AutomaticFeature
public class SUNECReplaceFeature extends JNIRegistrationUtil implements Feature {

    public static final String LIBENC_SUNEC_A = "libenc_sunec.a";
    private Path sunecTmpDir = null;

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        if (EnclaveOptions.RunInEnclave.getValue()) {
            access.registerReachabilityHandler(this::prepareEncSunEC,
                    method(access, "com.alibaba.confidentialcomputing.enclave.substitutes.NativeSunECMethods", "signDigest", byte[].class, byte[].class, byte[].class, byte[].class, int.class),
                    method(access, "com.alibaba.confidentialcomputing.enclave.substitutes.NativeSunECMethods", "verifySignedDigest", byte[].class, byte[].class, byte[].class, byte[].class),
                    method(access, "com.alibaba.confidentialcomputing.enclave.substitutes.NativeSunECMethods", "isCurveSupported", byte[].class),
                    method(access, "com.alibaba.confidentialcomputing.enclave.substitutes.NativeSunECMethods", "generateECKeyPair", int.class, byte[].class, byte[].class),
                    method(access, "com.alibaba.confidentialcomputing.enclave.substitutes.NativeSunECMethods", "deriveKey", byte[].class, byte[].class, byte[].class));
        }
    }

    @Platforms(Platform.LINUX.class)
    private void prepareEncSunEC(BeforeAnalysisAccess a) {
        if (Files.notExists(GRAALVM_HOME)) {
            VMError.shouldNotReachHere("System environment variable GRAALVM_HOME is set to " + GRAALVM_HOME
                    + ", but the directory does not exist!");
        }
        Path tempDirectory = ImageSingletons.lookup(TemporaryBuildDirectoryProvider.class).getTemporaryBuildDirectory();
        try {
            sunecTmpDir = tempDirectory.resolve("sunec");
            if (Files.notExists(sunecTmpDir)) {
                Files.createDirectory(sunecTmpDir);
            }
        } catch (IOException e) {
            VMError.shouldNotReachHere("Can't create sunec directory in tmp directory.", e);
        }

        prepareCFiles((FeatureImpl.BeforeAnalysisAccessImpl) a, sunecTmpDir);

        Path originalSunEC = GRAALVM_HOME.resolve("lib/static/linux-amd64/" + LibCBase.singleton().getName() + "/libsunec.a");
        if (Files.notExists(originalSunEC)) {
            VMError.shouldNotReachHere("Can't find original libsunec.a from $GRAALVM_HOME.");
        }
        List<String> command = new ArrayList<>();
        // 1. Compile the C symbol only ECC_JNI.c into ENC_ECC_JNI.o
        // LibCBase instance has been set in LibCFeature#afterRegistration, so it's safe to get it now.
        command.add(LibCBase.singleton().getTargetCompiler());
        command.add("-fPIC");
        command.add("-I" + GRAALVM_HOME.resolve("include").toAbsolutePath().toString());
        command.add("-I" + GRAALVM_HOME.resolve("include/linux").toAbsolutePath().toString());
        command.add("-I.");
        command.add("-L.");
        command.add("ECC_JNI.c");
        command.add("-c");
        command.add("-o");
        command.add("ENC_ECC_JNI.o");
        NativeCommandUtil.executeNewProcess(command, sunecTmpDir);

        // 2. Extract the original libsunec.a
        command.clear();
        command.add("ar");
        command.add("-x");
        command.add(originalSunEC.toAbsolutePath().toString());
        NativeCommandUtil.executeNewProcess(command, sunecTmpDir);

        // 3. Archive the ENC_ECC_JNI.o generated in step 1 and other original .o files into the new static library.
        command.clear();
        // Must in the form of "/bin/bash -c", otherwise the *.o is not recognized by ProcessBuilder.
        command.add("/bin/bash");
        command.add("-c");
        command.add("ar rcs " + LIBENC_SUNEC_A + " *.o");
        NativeCommandUtil.executeNewProcess(command, sunecTmpDir);

        // 4. Delete the intermediate results and only keep the static library generated by step 3.
        try {
            Files.walkFileTree(sunecTmpDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!file.getFileName().toString().endsWith(".a")) {
                        Files.delete(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            VMError.shouldNotReachHere("Fail to delete temporary file for generating substituted libsunec.a", e);
        }
    }

    private static void prepareCFiles(FeatureImpl.BeforeAnalysisAccessImpl access, Path sunecTmpDir) {
        ImageClassLoader imageClassLoader = access.getImageClassLoader();

        String dir = "native/sunec/";
        List<String> cSources = new ArrayList<>();
        cSources.add("com_alibaba_confidentialcomputing_enclave_substitutes_NativeSunECMethods.h");
        cSources.add("ecc_impl.h");
        cSources.add("ECC_JNI.c");
        cSources.add("ecl-exp.h");
        cSources.add("jlong.h");
        cSources.add("jlong_md.h");
        cSources.add("jni_util.h");

        for (String cSource : cSources) {
            File file = sunecTmpDir.resolve(cSource).toFile();

            try (InputStream inputStream = imageClassLoader.getClassLoader().getResourceAsStream(dir + cSource);
                 FileOutputStream outputStream = new FileOutputStream(file)) {
                if (inputStream == null) {
                    VMError.shouldNotReachHere("Source file " + cSource
                            + " doesn't exist. It is required to compile the enclave compatible libsunec.a.");
                }
                inputStream.transferTo(outputStream);
                outputStream.flush();
            } catch (IOException e) {
                VMError.shouldNotReachHere("Can't store resource file to tmp directory", e);
            }
        }
    }

    /**
     * Copy the new static library from temporary directory to the output directory.
     */
    @Override
    public void afterImageWrite(AfterImageWriteAccess access) {
        // The sunec methods are not reachable, so the new library doesn't exist
        if (sunecTmpDir == null) {
            return;
        }
        FeatureImpl.AfterImageWriteAccessImpl a = (FeatureImpl.AfterImageWriteAccessImpl) access;
        Path outputDirectory = NativeImageGenerator.generatedFiles(a.getUniverse().getBigBang().getOptions());

        Path sunecLibrary = sunecTmpDir.resolve(LIBENC_SUNEC_A);
        if (Files.exists(sunecLibrary)) {
            try {
                Files.copy(sunecLibrary, outputDirectory.resolve(LIBENC_SUNEC_A), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                VMError.shouldNotReachHere("Fail to copy file from temporary directory", e);
            }
        }
    }
}
