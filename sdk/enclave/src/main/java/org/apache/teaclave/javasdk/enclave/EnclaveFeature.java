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

package org.apache.teaclave.javasdk.enclave;

import org.apache.teaclave.javasdk.common.annotations.EnclaveMethod;
import org.apache.teaclave.javasdk.common.annotations.EnclaveService;
import org.apache.teaclave.javasdk.common.exception.ConfidentialComputingException;
import org.apache.teaclave.javasdk.enclave.framework.LoadServiceInvoker;
import org.apache.teaclave.javasdk.enclave.framework.ServiceMethodInvoker;
import org.apache.teaclave.javasdk.enclave.framework.UnloadServiceInvoker;
import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.core.c.libc.TemporaryBuildDirectoryProvider;
import com.oracle.svm.core.jdk.resources.NativeImageResourceFileSystemUtil;
import com.oracle.svm.core.util.VMError;
import com.oracle.svm.hosted.FeatureHandler;
import com.oracle.svm.hosted.FeatureImpl;
import com.oracle.svm.hosted.ImageClassLoader;
import com.oracle.svm.hosted.NativeImageGenerator;
import com.oracle.svm.hosted.ServiceLoaderFeature;
import com.oracle.svm.reflect.hosted.ReflectionFeature;
import com.oracle.svm.reflect.serialize.hosted.SerializationFeature;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;
import org.graalvm.nativeimage.hosted.RuntimeSerialization;
import org.graalvm.nativeimage.impl.RuntimeClassInitializationSupport;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@AutomaticFeature
public class EnclaveFeature implements Feature {

    private ImageClassLoader imageClassLoader;
    private final Map<Class<?>, Boolean> serializationCandidateTypes = new HashMap<>();
    private final Map<Class<?>, Boolean> reflectionCandidateTypes = new HashMap<>();
    private final Map<Method, Boolean> reflectionCandidateMethods = new HashMap<>();

    @Override
    public List<Class<? extends Feature>> getRequiredFeatures() {
        return Arrays.asList(ReflectionFeature.class, SerializationFeature.class, ServiceLoaderFeature.class);
    }

    /**
     * {@code com.oracle.svm.core.cpufeature.RuntimeCPUFeatureCheckFeature} is introduced since GraalVM 22.1.0. It is not
     * compatible with TEE SDK, so we have to disable it.
     *
     */
    @Override
    public void afterRegistration(AfterRegistrationAccess access) {
        if (EnclaveOptions.RunInEnclave.getValue()) {
            FeatureImpl.AfterRegistrationAccessImpl a = (FeatureImpl.AfterRegistrationAccessImpl) access;
            FeatureHandler featureHandler = a.getFeatureHandler();
            EnclavePlatFormSettings.disableFeatures(featureHandler, "com.oracle.svm.core.cpufeature.RuntimeCPUFeatureCheckFeature");
        }
    }

    @Override
    public void duringSetup(DuringSetupAccess access) {
        ImageSingletons.add(ServiceMethodInvoker.class, new ServiceMethodInvoker());
        ImageSingletons.add(LoadServiceInvoker.class, new LoadServiceInvoker());
        ImageSingletons.add(UnloadServiceInvoker.class, new UnloadServiceInvoker());
        ImageSingletons.lookup(RuntimeClassInitializationSupport.class).initializeAtBuildTime("org.apache.teaclave.javasdk.enclave.EnclavePrologue",
                "Prologue class should be initialize at build time.");

        FeatureImpl.DuringSetupAccessImpl config = (FeatureImpl.DuringSetupAccessImpl) access;
        RuntimeSerialization.register(ConfidentialComputingException.class, RuntimeException.class,
                ReflectiveOperationException.class, ClassNotFoundException.class);
        RuntimeSerialization.registerIncludingAssociatedClasses(Collections.EMPTY_LIST.getClass());
        imageClassLoader = config.getImageClassLoader();
    }

    /**
     * Collect reflection and serialization configurations from {@link EnclaveService} marked interfaces.
     */
    @Override
    public void duringAnalysis(DuringAnalysisAccess access) {
        List<Class<?>> enclaveServices = imageClassLoader.findAnnotatedClasses(EnclaveService.class, true);
        enclaveServices.forEach(serviceClazz -> {
            reflectionCandidateTypes.putIfAbsent(serviceClazz, false);
            byte[] serviceConfig = NativeImageResourceFileSystemUtil.getBytes("META-INF/services/" + serviceClazz.getName(), true);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(serviceConfig), StandardCharsets.UTF_8))) {
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    Class<?> implementation = imageClassLoader.findClass(line).get();
                    collectConfigs(implementation, Arrays.stream(implementation.getMethods()).filter(method ->
                            serviceClazz.isAssignableFrom(method.getDeclaringClass())
                    ).collect(Collectors.toList()));
                }
            } catch (IOException e) {
                VMError.shouldNotReachHere(e);
            }
        });
        List<Method> extraEnclaveMethods = imageClassLoader.findAnnotatedMethods(EnclaveMethod.class);
        extraEnclaveMethods.forEach(method -> collectConfigs(method.getDeclaringClass(), List.of(method)));

        // Register all newly collected configures
        if (registerCollectedConfigs()) {
            access.requireAnalysisIteration();
        }
    }

    /**
     * Copy the relocatable file and header file from temporary directory to output path.
     */
    @Override
    public void afterImageWrite(AfterImageWriteAccess access) {
        FeatureImpl.AfterImageWriteAccessImpl a = (FeatureImpl.AfterImageWriteAccessImpl) access;
        Path outputDirectory = NativeImageGenerator.generatedFiles(a.getUniverse().getBigBang().getOptions());
        Path tempDirectory = ImageSingletons.lookup(TemporaryBuildDirectoryProvider.class).getTemporaryBuildDirectory();
        try {
            if (Files.notExists(outputDirectory)) {
                Files.createDirectory(outputDirectory);
            }
            Files.walkFileTree(tempDirectory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String fileName = file.getFileName().toString();
                    if (fileName.endsWith(".o") || fileName.endsWith(".h")) {
                        Path target = outputDirectory.resolve(fileName).toAbsolutePath();
                        Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            VMError.shouldNotReachHere("Fail to copy file from temporary", e);
        }
    }

    private void collectConfigs(Class<?> clazz, List<Method> methods) {
        reflectionCandidateTypes.putIfAbsent(clazz, false);
        methods.stream().filter(m -> !Modifier.isStatic(m.getModifiers())).forEach(
                method -> {
                    for (Class<?> pType : method.getParameterTypes()) {
                        serializationCandidateTypes.putIfAbsent(pType, false);
                    }
                    serializationCandidateTypes.putIfAbsent(method.getReturnType(), false);
                    for (Class<?> expType : method.getExceptionTypes()) {
                        serializationCandidateTypes.putIfAbsent(expType, false);
                    }
                    reflectionCandidateMethods.putIfAbsent(method, false);
                }
        );
    }

    private boolean registerCollectedConfigs() {
        boolean registeredNewSerializations = registerCollectedConfigs(serializationCandidateTypes, RuntimeSerialization::registerIncludingAssociatedClasses);
        boolean registeredNewReflectionTypes = registerCollectedConfigs(reflectionCandidateTypes, RuntimeReflection::register);
        boolean registeredNewReflectionMethods = registerCollectedConfigs(reflectionCandidateMethods, RuntimeReflection::register);
        return registeredNewSerializations || registeredNewReflectionTypes || registeredNewReflectionMethods;
    }

    private <T> boolean registerCollectedConfigs(Map<T, Boolean> configs, Consumer<T> registerAction) {
        boolean needRegisterNew = configs.entrySet().stream().anyMatch(entry -> !entry.getValue());
        configs.entrySet().stream().filter(entry -> !entry.getValue()).map(Map.Entry::getKey).forEach(
                key -> {
                    registerAction.accept(key);
                    configs.put(key, true);
                }
        );
        return needRegisterNew;
    }
}
