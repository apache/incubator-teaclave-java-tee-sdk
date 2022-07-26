package com.alibaba.enclave.bouncycatsle;

import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.core.configure.ConfigurationFiles;
import com.oracle.svm.core.option.HostedOptionValues;
import com.oracle.svm.core.util.VMError;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.graalvm.collections.EconomicMap;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.graalvm.nativeimage.impl.RuntimeClassInitializationSupport;

import java.nio.file.Path;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.alibaba.enclave.bouncycatsle.BCOptions.RegisterBCProvider;
import static com.oracle.svm.hosted.SecurityServicesFeature.Options.AdditionalSecurityProviders;

/**
 * This feature set the configurations, class initialization states and necessary providers to support building native
 * image with BouncyCastle libraries.
 */
@AutomaticFeature
public class BouncyCastleFeature implements Feature {
    private static final String PATH_FORMAT = "configs/%s/%s/%s-config.json";
    private static final List<String> BC_MODULES = Arrays.asList("pg", "prov", "pkix", "core", "tls", "util", "mail");
    private static final String DRBG_DEFAULT = "org.bouncycastle.jcajce.provider.drbg.DRBG$Default";
    private static final String DRBG_NONCE_AND_IV = "org.bouncycastle.jcajce.provider.drbg.DRBG$NonceAndIV";
    private String version;

    @Override
    public void afterRegistration(AfterRegistrationAccess access) {
        try {
            List<String> dependBCModules = extractJarInfo(access);
            // Bouncycastle core is always required.
            ConfigurationFiles.Options.ReflectionConfigurationResources.update(getOptionsMap(), String.format(PATH_FORMAT, version, "core", "reflect"));
            dependBCModules.forEach(module -> {
                ConfigurationFiles.Options.ReflectionConfigurationResources.update(getOptionsMap(), String.format(PATH_FORMAT, version, module, "reflect"));
                ConfigurationFiles.Options.SerializationConfigurationResources.update(getOptionsMap(), String.format(PATH_FORMAT, version, module, "serialization"));
            });
        } catch (BouncycastleJarNameFormatException e) {
            VMError.shouldNotReachHere(e);
        }
        if (RegisterBCProvider.getValue()) {
            // Must register initialization first and then explicitly add the BouncyCastleProvider to the security provider list,
            // so that it can be seen later at build time.
            RuntimeClassInitialization.initializeAtBuildTime("org.bouncycastle");
            Security.addProvider(new BouncyCastleProvider());
            AdditionalSecurityProviders.update(getOptionsMap(), BouncyCastleProvider.class.getName());
        }
    }

    private List<String> extractJarInfo(AfterRegistrationAccess access) throws BouncycastleJarNameFormatException {
        List<String> ret = new ArrayList<>();
        for (Path p : access.getApplicationClassPath()) {
            String s = p.getFileName().toString();
            if (s.endsWith(".jar")) {
                for (String m : BC_MODULES) {
                    if (s.startsWith("bc" + m + "-")) {
                        //bouncycastle jar name format is "bc[module]-[jdkversion]-[bcversion].jar"
                        int lastdot = s.lastIndexOf('.');
                        String jarName = s.substring(0, lastdot);
                        String[] nameElements = jarName.split("-");

                        String versionCandidate = nameElements[2];
                        if (versionCandidate.matches("\\d*\\.?\\d*")) {
                            if (versionCandidate.indexOf('.') == -1) {
                                int v = Integer.parseInt(versionCandidate);
                                versionCandidate = Float.toString(v / 100f);
                            }
                            version = versionCandidate;
                        } else {
                            throw new BouncycastleJarNameFormatException(String.format("bouncycastle jar file %s does not follow bc[module]-[jdkversion]-[bcversion]<-others>.jar", jarName));
                        }
                        ret.add(m);
                    }
                }
            }
        }
        return ret;
    }

    private static EconomicMap getOptionsMap() {
        return (EconomicMap) HostedOptionValues.singleton().getMap();
    }

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        if (RegisterBCProvider.getValue()) {
            RuntimeClassInitializationSupport rcis = ImageSingletons.lookup(RuntimeClassInitializationSupport.class);
            String reason = "BouncyCastleProvider related should be initialized at runtime";
            rcis.initializeAtRunTime(access.findClassByName(DRBG_DEFAULT), reason);
            rcis.initializeAtRunTime(access.findClassByName(DRBG_NONCE_AND_IV), reason);
        }
    }
}
