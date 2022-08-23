package com.alibaba.confidentialcomputing.host;

import com.alibaba.confidentialcomputing.common.*;
import com.alibaba.confidentialcomputing.common.exception.ConfidentialComputingException;
import com.alibaba.confidentialcomputing.host.exception.*;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

class MockTestEnclave extends AbstractEnclave {
    private static final AtomicLong instanceIdentity = new AtomicLong(0);
    private static final Map<String, Object> instancesRegisterCenter = new ConcurrentHashMap<>();
    private static final Queue<ServiceHandler> cacheServiceHandler = new LinkedList<>();

    MockTestEnclave() throws EnclaveCreatingException {
        super(EnclaveType.NONE, EnclaveDebug.NONE, new BaseEnclaveServicesRecycler());
    }

    private static Class<?>[] parseParamClass(String[] parameterTypes) {
        if (parameterTypes == null) {
            return null;
        }
        List<Class<?>> parametersClass = new ArrayList<>();
        Arrays.stream(parameterTypes).forEach(p -> {
            try {
                parametersClass.add(nameToType(p));
            } catch (ClassNotFoundException e) {
                fail();
            }
        });
        return parametersClass.toArray(new Class<?>[0]);
    }

    private static Class<?> nameToType(String name) throws ClassNotFoundException {
        if (name.indexOf('.') == -1) {
            switch (name) {
                case "boolean":
                    return boolean.class;
                case "char":
                    return char.class;
                case "float":
                    return float.class;
                case "double":
                    return double.class;
                case "byte":
                    return byte.class;
                case "short":
                    return short.class;
                case "int":
                    return int.class;
                case "long":
                    return long.class;
                case "void":
                    return void.class;
            }
        }
        return Class.forName(name);
    }

    @Override
    AttestationReport generateAttestationReportNative(byte[] userData) throws RemoteAttestationException {
        throw new RemoteAttestationException("MockTestEnclave enclave doesn't support remote attestation generation.");
    }

    static int verifyAttestationReport(byte[] ignoredReport) throws RemoteAttestationException {
        throw new RemoteAttestationException("MockTestEnclave enclave doesn't support remote attestation verification.");
    }

    @Override
    byte[] loadServiceNative(String interfaceName) throws ServicesLoadingException {
        List<ServiceHandler> handlers = new ArrayList<>();
        Throwable exception = null;
        EnclaveInvocationResult result;
        try {
            Class<?> service = Class.forName(interfaceName);
            for (Object o : ServiceLoader.load(service)) {
                String identity = String.valueOf(instanceIdentity.addAndGet(1));
                ServiceHandler sm = new ServiceHandler(interfaceName, o.getClass().getName(), identity);
                handlers.add(sm);
                cacheServiceHandler.add(sm);
                instancesRegisterCenter.put(identity, o);
            }
        } catch (ClassNotFoundException e) {
            exception = e;
        } finally {
            result = new EnclaveInvocationResult(handlers.toArray(new ServiceHandler[0]), exception);
        }

        try {
            return SerializationHelper.serialize(result);
        } catch (IOException e) {
            throw new ServicesLoadingException(e);
        }
    }

    @Override
    byte[] unloadServiceNative(ServiceHandler handler) throws ServicesUnloadingException {
        EnclaveInvocationResult result;

        instancesRegisterCenter.remove(handler.getInstanceIdentity());
        result = new EnclaveInvocationResult(null, null);

        try {
            return SerializationHelper.serialize(result);
        } catch (IOException e) {
            throw new ServicesUnloadingException(e);
        }
    }

    @Override
    byte[] invokeMethodNative(EnclaveInvocationContext invocationContext) throws EnclaveMethodInvokingException {
        Throwable exception = null;
        Object invokeRet = null;
        EnclaveInvocationResult result;
        try {
            String className = invocationContext.getServiceHandler().getServiceImplClassName();
            String[] parameterTypes = invocationContext.getParameterTypes();
            String methodName = invocationContext.getMethodName();
            Object[] args = invocationContext.getArguments();
            Object instance = instancesRegisterCenter.get(invocationContext.getServiceHandler().getInstanceIdentity());
            assertNotNull(instance);
            assertEquals(className, instance.getClass().getName());
            Class<?> service = Class.forName(className);
            Method method = service.getDeclaredMethod(methodName, parseParamClass(parameterTypes));
            method.setAccessible(true);
            invokeRet = method.invoke(instance, args);
        } catch (Throwable e) {
            exception = new ConfidentialComputingException(e);
        } finally {
            result = new EnclaveInvocationResult(invokeRet, exception);
        }

        try {
            return SerializationHelper.serialize(result);
        } catch (IOException e) {
            throw new EnclaveMethodInvokingException(e);
        }
    }

    @Override
    public EnclaveInfo getEnclaveInfo() {
        return null;
    }

    @Override
    public void destroy() {
        // destroyToken will wait for all ongoing enclave invocations finished.
        if (this.getEnclaveContext().getEnclaveToken().destroyToken()) {
            // interrupt enclave services' recycler firstly.
            this.getEnclaveContext().getEnclaveServicesRecycler().interruptServiceRecycler();
        }
    }

    int getServicesNum() {
        return instancesRegisterCenter.size();
    }

    Queue<?> getCachedServiceHandler() {
        return cacheServiceHandler;
    }
}