package com.alibaba.confidentialcomputing.enclave;

import com.alibaba.confidentialcomputing.common.annotations.EnclaveMethod;
import com.alibaba.confidentialcomputing.common.annotations.EnclaveService;
import com.alibaba.confidentialcomputing.common.exception.ConfidentialComputingException;
import com.alibaba.confidentialcomputing.enclave.framework.LoadServiceInvoker;
import com.alibaba.confidentialcomputing.enclave.framework.ServiceMethodInvoker;
import com.alibaba.confidentialcomputing.enclave.framework.UnloadServiceInvoker;
import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.core.jdk.resources.NativeImageResourceFileSystemUtil;
import com.oracle.svm.core.util.VMError;
import com.oracle.svm.hosted.FeatureImpl;
import com.oracle.svm.hosted.ImageClassLoader;
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

    @Override
    public void duringSetup(DuringSetupAccess access) {
        ImageSingletons.add(ServiceMethodInvoker.class, new ServiceMethodInvoker());
        ImageSingletons.add(LoadServiceInvoker.class, new LoadServiceInvoker());
        ImageSingletons.add(UnloadServiceInvoker.class, new UnloadServiceInvoker());
        ImageSingletons.lookup(RuntimeClassInitializationSupport.class).initializeAtBuildTime("com.alibaba.confidentialcomputing.enclave.EnclavePrologue",
                "Prologue class should be initialize at build time.");

        FeatureImpl.DuringSetupAccessImpl config = (FeatureImpl.DuringSetupAccessImpl) access;
        RuntimeSerialization.register(ConfidentialComputingException.class, RuntimeException.class,
                ReflectiveOperationException.class, ClassNotFoundException.class);
        RuntimeSerialization.registerAllAssociatedClasses(Collections.EMPTY_LIST.getClass());
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
        boolean registeredNewSerializations = registerCollectedConfigs(serializationCandidateTypes, RuntimeSerialization::registerAllAssociatedClasses);
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
