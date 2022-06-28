package com.alibaba.confidentialcomputing.host;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.concurrent.*;

import com.alibaba.confidentialcomputing.common.*;
import com.alibaba.confidentialcomputing.host.exception.*;

/**
 * EmbeddedLibOSEnclave is a sgx2 enclave based on Ant's Occlum libos.
 * EmbeddedLibOSEnclave is a singleton object module, there is only one
 * EmbeddedLibOSEnclave object in a process.
 */
public class EmbeddedLibOSEnclave extends AbstractEnclave {
    private static final int HTTP_CONNECT_TIMEOUT_MS = 50; // ms.
    private static final int HTTP_READ_TIMEOUT_MS = 200;   // ms.
    private static final int HTTP_READ_REMOTE_ATTESTATION_TIMEOUT_MS = HTTP_READ_TIMEOUT_MS * 10;  // ms.
    private static final String EMBEDDED_LIB_OS_ENCLAVE_STARTUP_THREAD_NAME = "async_lib_os_enclave_startup_thread";
    private static final String HTTP_SERVER_PREFIX = "http://localhost:";
    private static final String HTTP_SERVER_NAME = "/enclaveAgent";
    private final static String JNI_EXTRACTED_PACKAGE_PATH = "jni/lib_jni_embedded_lib_os_enclave.so";
    private final static String EMBEDDED_LIB_OS_ENCLAVE_SIGNED_PACKAGE_PATH = "lib_embedded_lib_os_enclave_load.tgz";
    private final static String EMBEDDED_LIB_OS_ENCLAVE_SIGNED_PACKAGE_PATH_TAIL = "occlum_instance";
    private static volatile LibOSExtractTempPath extractTempPath;
    private static volatile EmbeddedLibOSEnclave singleInstance;

    // enclaveHandle stores created enclave's handle id.
    private long enclaveHandle;
    private int portHost;
    private int portEnclave;
    private URL url;
    private String httpURL;

    static EmbeddedLibOSEnclave getEmbeddedLibOSEnclaveInstance(EnclaveDebug mode, EnclaveSimulate sim) throws EnclaveCreatingException {
        synchronized (EmbeddedLibOSEnclave.class) {
            if (singleInstance == null) {
                singleInstance = new EmbeddedLibOSEnclave(mode, sim);
            }
            return singleInstance;
        }
    }

    private EmbeddedLibOSEnclave(EnclaveDebug mode, EnclaveSimulate sim) throws EnclaveCreatingException {
        // Set EnclaveContext for this enclave instance.
        super(EnclaveType.EMBEDDED_LIB_OS, mode, new EnclaveServicesRecycler());
        // Extract jni .so and signed tee .so from .jar file.
        // Only once extract and load operation.
        if (extractTempPath == null) {
            synchronized (EmbeddedLibOSEnclave.class) {
                if (extractTempPath == null) {
                    try {
                        String jniTempFilePath = ExtractLibrary.extractLibrary(
                                EmbeddedLibOSEnclave.class.getClassLoader(),
                                JNI_EXTRACTED_PACKAGE_PATH);
                        String embeddedLibOsSignedFilePath = ExtractLibrary.extractAndDeCompressTgz(
                                EmbeddedLibOSEnclave.class.getClassLoader(),
                                EMBEDDED_LIB_OS_ENCLAVE_SIGNED_PACKAGE_PATH) + "/" + EMBEDDED_LIB_OS_ENCLAVE_SIGNED_PACKAGE_PATH_TAIL;
                        extractTempPath = new EmbeddedLibOSEnclave.LibOSExtractTempPath(jniTempFilePath, embeddedLibOsSignedFilePath);
                        System.load(extractTempPath.getJniTempFilePath());
                        registerNatives();
                    } catch (IOException e) {
                        throw new EnclaveCreatingException("extracting embedded lib os enclave jni .so or signed .so failed.", e);
                    }
                }
            }
        }

        try {
            portHost = getFreePort();
            portEnclave = getFreePort();
            httpURL = HTTP_SERVER_PREFIX + portEnclave + HTTP_SERVER_NAME;
            url = new URL(httpURL);
            // Attach to target enclave service by rmi.
            attachToEnclaveAgent(mode, sim);
        } catch (IOException e) {
            throw new EnclaveCreatingException(e);
        }
    }

    // apply a free port for localhost communication between host and enclave.
    private int getFreePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }

    private Future<EnclaveCreatingException> startupLibOSEnclaveAsync(EnclaveDebug mode, EnclaveSimulate sim) {
        // Create embedded lib os enclave by native call asynchronously.
        // Occlum embedded start up interface is occlum_pal_exec, it blocks until progress exit in enclave.
        return Executors.newFixedThreadPool(1, new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName(EMBEDDED_LIB_OS_ENCLAVE_STARTUP_THREAD_NAME);
                thread.setDaemon(true);
                return thread;
            }
        }).submit(() -> {
            EnclaveCreatingException exception = null;
            try {
                nativeCreateEnclave(
                        mode.getValue(),
                        sim.getValue(),
                        portHost,
                        portEnclave,
                        EmbeddedLibOSEnclaveConfig.getEmbeddedLibOSEnclaveConfigInstance(),
                        extractTempPath.getLibOSSignedFilePath());
            } catch (EnclaveCreatingException e) {
                exception = e;
            }
            return exception;
        });
    }

    // wait for enclave jvm start up and notify host.
    private void waitForEnclaveStartup() throws IOException {
        try (ServerSocket server = new ServerSocket(this.portHost)) {
            server.setSoTimeout(EmbeddedLibOSEnclaveConfig.getEmbeddedLibOSEnclaveConfigInstance().getEmbeddedLibOSEnclaveStartupDuration());
            server.accept();
        }
    }

    // attach to enclave embedded lib os java service.
    private void attachToEnclaveAgent(EnclaveDebug mode, EnclaveSimulate sim) throws EnclaveCreatingException {
        startupLibOSEnclaveAsync(mode, sim);
        try {
            waitForEnclaveStartup();
        } catch (IOException e) {
            throw new EnclaveCreatingException(e);
        }
    }

    private static native void registerNatives();

    private native int nativeCreateEnclave(int mode, int sim, int portHost, int portEnclave, EmbeddedLibOSEnclaveConfig config, String path) throws EnclaveCreatingException;

    private native int nativeDestroyEnclave(long enclaveHandler) throws EnclaveDestroyingException;

    private byte[] remoteRequest(byte[] request, int connectTimeout, int inTimeout) throws IOException, InterruptedException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(inTimeout);
        conn.connect();

        try (OutputStream outputStream = conn.getOutputStream()) {
            outputStream.write(request);
            outputStream.flush();
        }

        try (InputStream inputStream = conn.getInputStream()) {
            return inputStream.readAllBytes();
        }
    }

    @Override
    byte[] loadServiceNative(String service) throws ServicesLoadingException {
        try {
            SocketEnclaveInvocationContext context =
                    new SocketEnclaveInvocationContext(SocketEnclaveInvocationContext.SERVICE_LOADING, new ServiceHandler(service));
            return remoteRequest(SerializationHelper.serialize(context), HTTP_CONNECT_TIMEOUT_MS, HTTP_READ_TIMEOUT_MS);
        } catch (InterruptedException | IOException e) {
            throw new ServicesLoadingException(e);
        }
    }

    @Override
    byte[] unloadServiceNative(ServiceHandler handler) throws ServicesUnloadingException {
        try {
            SocketEnclaveInvocationContext context =
                    new SocketEnclaveInvocationContext(SocketEnclaveInvocationContext.SERVICE_UNLOADING, handler);
            return remoteRequest(SerializationHelper.serialize(context), HTTP_CONNECT_TIMEOUT_MS, HTTP_READ_TIMEOUT_MS);
        } catch (InterruptedException | IOException e) {
            throw new ServicesUnloadingException(e);
        }
    }

    @Override
    byte[] invokeMethodNative(EnclaveInvocationContext service) throws EnclaveMethodInvokingException {
        try {
            SocketEnclaveInvocationContext context =
                    new SocketEnclaveInvocationContext(SocketEnclaveInvocationContext.METHOD_INVOCATION, service);
            // Should not set http timeout parameter in method invoke, the duration is deeply depends on user service.
            return remoteRequest(SerializationHelper.serialize(context), HTTP_CONNECT_TIMEOUT_MS, 0x0);
        } catch (InterruptedException | IOException e) {
            throw new EnclaveMethodInvokingException(e);
        }
    }

    @Override
    AttestationReport generateAttestationReportNative(byte[] userData) throws RemoteAttestationException {
        try {
            SocketEnclaveInvocationContext context =
                    new SocketEnclaveInvocationContext(SocketEnclaveInvocationContext.REMOTE_ATTESTATION_GENERATE, userData);
            EnclaveInvocationResult resultWrapper = (EnclaveInvocationResult) SerializationHelper.deserialize(
                    remoteRequest(SerializationHelper.serialize(context), HTTP_CONNECT_TIMEOUT_MS, HTTP_READ_REMOTE_ATTESTATION_TIMEOUT_MS));
            if (resultWrapper.getException() != null) {
                throw resultWrapper.getException();
            }
            EmbeddedLibOSInnerAttestationReport report = (EmbeddedLibOSInnerAttestationReport) resultWrapper.getResult();
            return new EmbeddedLibOSAttestationReport(
                    report.getQuote(),
                    report.getMrSigner(),
                    report.getMrEnclave(),
                    report.getUserData());
        } catch (InterruptedException | IOException | ClassNotFoundException e) {
            throw new RemoteAttestationException(e);
        } catch (Throwable e) {
            throw new RemoteAttestationException(e);
        }
    }

    static int verifyAttestationReport(byte[] quote) throws RemoteAttestationException {
        return SGXRemoteAttestationVerify.VerifyAttestationReport(quote);
    }

    @Override
    public void destroy() throws EnclaveDestroyingException {
        synchronized (EmbeddedLibOSEnclave.class) {
            // Because enclave libos occlum doesn't support creating a new occlum instance even
            // destroy the pre-created occlum instance, Do nothing here.
            // embedded lib os occlum instance in JavaEnclave is similar with a singleton instance.
        }
    }

    class LibOSExtractTempPath {
        private final String jniTempFilePath;
        private final String libOsSignedFilePath;

        LibOSExtractTempPath(String jniTempFilePath, String teeSdkSignedFilePath) {
            this.jniTempFilePath = jniTempFilePath;
            this.libOsSignedFilePath = teeSdkSignedFilePath;
        }

        String getJniTempFilePath() {
            return jniTempFilePath;
        }

        String getLibOSSignedFilePath() {
            return libOsSignedFilePath;
        }
    }
}
