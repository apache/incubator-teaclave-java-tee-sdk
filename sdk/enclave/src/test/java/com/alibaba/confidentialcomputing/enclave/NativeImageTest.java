package com.alibaba.confidentialcomputing.enclave;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.alibaba.confidentialcomputing.enclave.NativeCommandUtil.GRAALVM_HOME;
import static com.alibaba.confidentialcomputing.enclave.SUNECReplaceFeature.LIBENC_SUNEC_A;

public abstract class NativeImageTest implements NativeImageTestable {
    private static final String JNI_LIB_NAME = "encinvokeentrytest";
    public static final Path MVN_BUILD_DIR = Paths.get("target");
    private static final String SVM_OUT = "svm-out";
    private static final String SVM_ENCLAVE_LIB = "svm_enclave_sdk";

    private static final boolean useStaticLink = true;
    public static final String ENC_INVOKE_ENTRY_TEST_C = "enc_invoke_entry_test.c";

    static {
        if (!GRAALVM_HOME.toFile().exists()) {
            throw new RuntimeException("System environment variable GRAALVM_HOME is set to " + GRAALVM_HOME
                    + ", but the directory does not exist!");
        }
        if (!MVN_BUILD_DIR.toFile().exists()) {
            throw new RuntimeException("Maven default build directory " + MVN_BUILD_DIR.toAbsolutePath() + " doesn't exist." +
                    " Please check your maven's ${project.build.directory} property and make sure it's \"target\".");
        }
    }

    protected String testName;
    protected Path workingDir;
    protected Path configRootDir;
    protected Path configPathDir;
    protected Path svmEncSDKClassDir;
    protected Path serviceDir;
    protected Path svmOutputDir;
    protected Path testClassesDir;
    protected Path svmCompileClassesDir;

    static class SVMCompileElements {
        private final List<String> serviceConfigs = new ArrayList<>();
        private final List<String> svmConfigs = new ArrayList<>();
        private final List<String> otherResources = new ArrayList<>();
        private final List<Class<?>> classes = new ArrayList<>();

        public void addServices(String... services) {
            serviceConfigs.addAll(Arrays.asList(services));
        }

        public void addSvmConfigs(String... configs) {
            svmConfigs.addAll(Arrays.asList(configs));
        }

        public void addResources(String... resources) {
            otherResources.addAll(Arrays.asList(resources));
        }

        public void addClasses(Class<?>... classes2Compile) {
            classes.addAll(Arrays.asList(classes2Compile));
        }
    }

    public NativeImageTest() {
        TestTarget targetTest = this.getClass().getAnnotation(TestTarget.class);
        if (targetTest == null) {
            throw new RuntimeException("The subclasses of NativeImageTest must use @TestTarget specify the test target class."
                    + " But class " + this.getClass().getName() + " doesn't have one.");
        }
        String targetTestClassName = targetTest.value().getName();
        int lastDot = targetTestClassName.lastIndexOf('.');
        testName = targetTestClassName.substring(lastDot + 1).toLowerCase();

        workingDir = MVN_BUILD_DIR.resolve("native-work-dir-" + testName);
        configRootDir = workingDir.resolve("config");
        configPathDir = configRootDir.resolve("META-INF/native-image");
        svmEncSDKClassDir = MVN_BUILD_DIR.resolve("classes");
        serviceDir = configRootDir.resolve("META-INF/services");
        svmOutputDir = workingDir.resolve(SVM_OUT);
        svmCompileClassesDir = workingDir.resolve("bin");
        List<Path> dirsToCreate = new ArrayList<>();
        dirsToCreate.add(workingDir);
        dirsToCreate.add(configRootDir);
        dirsToCreate.add(configPathDir);
        dirsToCreate.add(serviceDir);
        dirsToCreate.add(svmCompileClassesDir);
        dirsToCreate.forEach(p -> createDirs(p, "Can't create directory " + p + " at test preparation time"));
        testClassesDir = MVN_BUILD_DIR.resolve("test-classes");
    }

    public void prepareNativeLibraries() {
        collectSVMCompileItems();
        runWithNativeImageAgent();
        beforeSVMCompile();
        svmCompile();
        afterSVMCompile();
        compileJNILibrary();
        System.load(workingDir.resolve("lib" + JNI_LIB_NAME + ".so").toAbsolutePath().toString());
    }

    private void collectSVMCompileItems() {
        SVMCompileElements items = specifyTestClasses();
        if (items == null) {
            throw new RuntimeException("Must specify the elements to be compiled by native-image for testing.");
        }

        if (!items.svmConfigs.isEmpty()) {
            items.svmConfigs.stream().map(s -> testClassesDir.resolve(s).toAbsolutePath()).
                    forEach(p -> copyFile(p, configPathDir.resolve(p.getFileName()), "Fail to copy configuration file."));
        }

        if (items.classes.isEmpty()) {
            throw new RuntimeException("Must specify the classes to be compiled by native-image for testing.");
        } else {
            items.classes.forEach(c ->
            {
                String classLocation = getClassFileName(c);
                Path destPath = svmCompileClassesDir.resolve(classLocation);
                createDirs(destPath.getParent(), "Can't create class directories for native-image compilation.");
                copyFile(testClassesDir.resolve(classLocation).toAbsolutePath(), destPath, "Can't copy class file.");
            });
        }

        if (!items.serviceConfigs.isEmpty()) {
            items.serviceConfigs.stream().map(s -> testClassesDir.resolve(s).toAbsolutePath()).
                    forEach(p -> copyFile(p, serviceDir.resolve(p.getFileName()), null));
        }

        if (!items.otherResources.isEmpty()) {
            items.otherResources.forEach(s -> {
                Path destPath = svmCompileClassesDir.resolve(s);
                createDirs(destPath.getParent(), "Can't create resource directories for native-image compilation.");
                copyFile(testClassesDir.resolve(s).toAbsolutePath(), destPath, "Can't copy resource file.");
            });
        }
    }

    abstract SVMCompileElements specifyTestClasses();

    protected void svmCompile() {
        List<String> command = new ArrayList<>();
        command.add(0, GRAALVM_HOME.resolve("bin/native-image").toString());
        command.add("-cp");
        StringBuilder sb = new StringBuilder();
        List<Path> svmBinFiles = Arrays.asList(svmCompileClassesDir,
                configRootDir,
                svmEncSDKClassDir,
                MVN_BUILD_DIR.toAbsolutePath().getParent().getParent().resolve("common/target/classes")
        );
        svmBinFiles.stream().map(p -> p.normalize().toAbsolutePath()).forEach(p -> {
            if (Files.notExists(p)) {
                throw new RuntimeException("File " + p + " on native-image class file doesn't exist.");
            }
            sb.append(p.toString()).append(File.pathSeparator);
        });
        command.add(sb.deleteCharAt(sb.length() - 1).toString());
        command.add("--shared");
        if (useStaticLink) {
            command.add("--libc=musl");
        }
        command.add("--no-fallback");
        command.add("-H:Path=" + SVM_OUT);
        command.add("-H:+RunInEnclave");
        command.add("-H:+ReportExceptionStackTraces");
        command.add("-H:Name=lib" + SVM_ENCLAVE_LIB);
        command.add("-H:-DeleteLocalSymbols");
        List<String> extraOptions = extraSVMOptions();
        if (extraOptions != null && !extraOptions.isEmpty()) {
            command.addAll(extraOptions);
        }
        NativeCommandUtil.executeNewProcess(command, workingDir);
    }

    private void compileJNILibrary() {
        System.out.println("###Prepare JNI library ...###");
        List<Path> requiredFilePaths = new ArrayList<>();
        requiredFilePaths.add(testClassesDir.resolve("native/com_alibaba_confidentialcomputing_enclave_EnclaveTestHelper.h"));
        requiredFilePaths.add(testClassesDir.resolve("native/" + ENC_INVOKE_ENTRY_TEST_C));
        requiredFilePaths.add(svmOutputDir.resolve("lib" + SVM_ENCLAVE_LIB + ".h"));
        requiredFilePaths.add(svmOutputDir.resolve("graal_isolate.h"));
        requiredFilePaths.add(svmOutputDir.resolve("enc_environment.h"));
        if (useStaticLink) {
            requiredFilePaths.add(svmOutputDir.resolve("lib" + SVM_ENCLAVE_LIB + ".o"));
        } else {
            requiredFilePaths.add(svmOutputDir.resolve("lib" + SVM_ENCLAVE_LIB + ".so"));
        }
        requiredFilePaths.forEach(p -> copyFile(p, workingDir.resolve(p.getFileName()), null));

        List<String> command = new ArrayList<>();
        if (useStaticLink) {
            prepareStaticLinkingCommand(command);
        } else {
            prepareDynamicLinkingCommand(command);
        }
        command.addAll(addMacros());
        NativeCommandUtil.executeNewProcess(command, workingDir);
    }

    protected Collection<String> addMacros(){
        return Collections.EMPTY_LIST;
    }

    private void prepareStaticLinkingCommand(List<String> command) {
        Path graalvmHome = GRAALVM_HOME.toAbsolutePath();
        command.add("gcc");
        command.add("-z");
        command.add("noexecstack");
        command.add("-fPIC");
        command.add("-I" + graalvmHome.resolve("include").toString());
        command.add("-I" + graalvmHome.resolve("include/linux").toString());
        command.add(ENC_INVOKE_ENTRY_TEST_C);
        command.add("lib" + SVM_ENCLAVE_LIB + ".o");
        command.add("-I.");
        command.add("-L.");
        command.add(graalvmHome.resolve("lib/svm/clibraries/linux-amd64/liblibchelper.a").toString());
        command.add(graalvmHome.resolve("lib/svm/clibraries/linux-amd64/libjvm.a").toString());
        command.add(graalvmHome.resolve("lib/static/linux-amd64/musl/libnio.a").toString());
        command.add(graalvmHome.resolve("lib/static/linux-amd64/musl/libzip.a").toString());
        command.add(graalvmHome.resolve("lib/static/linux-amd64/musl/libnet.a").toString());
        command.add(graalvmHome.resolve("lib/static/linux-amd64/musl/libjava.a").toString());
        if (Files.exists(svmOutputDir.toAbsolutePath().resolve(LIBENC_SUNEC_A))) {
            command.add(svmOutputDir.toAbsolutePath().resolve(LIBENC_SUNEC_A).toString());
        }
        command.add(graalvmHome.resolve("lib/static/linux-amd64/musl/libfdlibm.a").toString());
        command.add("-std=c99");
        command.add("-lc");
        command.add("-shared");
        command.add("-o");
        command.add("lib" + JNI_LIB_NAME + ".so");
    }

    private void prepareDynamicLinkingCommand(List<String> command) {
        command.add("gcc");
        command.add("-fPIC");
        command.add("-I" + GRAALVM_HOME.toAbsolutePath() + "/include");
        command.add("-I" + GRAALVM_HOME.toAbsolutePath() + "/include/linux");
        command.add(ENC_INVOKE_ENTRY_TEST_C);
        command.add("-I.");
        command.add("-L.");
        command.add("-std=c99");
        command.add("-l" + SVM_ENCLAVE_LIB);
        command.add("-lc");
        command.add("-shared");
        command.add("-o");
        command.add("lib" + JNI_LIB_NAME + ".so");
    }


    public static void copyFile(Path source, Path dest, String errMSg) {
        try {
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(errMSg, e);
        }
    }

    private static void createDirs(Path p, String errMsg) {
        if (Files.notExists(p)) {
            try {
                Files.createDirectories(p);
            } catch (IOException e) {
                throw new RuntimeException(errMsg, e);
            }
        }
    }

    public static String getClassFileName(Class<?> clazz) {
        return clazz.getName().replace('.', '/') + ".class";
    }
}
