package com.alibaba.confidentialcomputing.test.host;

import com.alibaba.confidentialcomputing.host.*;
import org.junit.jupiter.api.Test;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.remote.*;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestEnclaveInfoMXBean {
    private static final String DOMAIN_NAME = "EnclaveMXBean";
    private static final String ENCLAVE_MX_BEAN_STUB = "enclaveInfoMXBeanStub";

    private CountDownLatch cl0 = new CountDownLatch(1);
    private CountDownLatch cl1 = new CountDownLatch(1);

    private int rmiPort = getFreePort();
    ObjectName enclaveInfoMXBeanStub;
    private Registry registry;
    private MBeanServer mxBeanService;
    private JMXConnectorServer jmxConnector;

    private int getFreePort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void service() throws Exception {
        Enclave enclaveJVM = EnclaveFactory.create(EnclaveType.MOCK_IN_JVM);
        EnclaveInfo enclaveInfoJVM = enclaveJVM.getEnclaveInfo();
        assertEquals(enclaveInfoJVM.getEnclaveType(), EnclaveType.MOCK_IN_JVM);
        assertEquals(enclaveInfoJVM.isEnclaveDebuggable(), true);
        assertEquals(enclaveInfoJVM.getEnclaveEPCMemorySizeBytes(), -1);
        assertEquals(enclaveInfoJVM.getEnclaveMaxThreadsNumber(), -1);

        Enclave enclaveSVM = EnclaveFactory.create(EnclaveType.MOCK_IN_SVM);
        EnclaveInfo enclaveInfoSVM = enclaveSVM.getEnclaveInfo();
        assertEquals(enclaveInfoSVM.getEnclaveType(), EnclaveType.MOCK_IN_SVM);
        assertEquals(enclaveInfoSVM.isEnclaveDebuggable(), true);
        assertEquals(enclaveInfoSVM.getEnclaveEPCMemorySizeBytes(), -1);
        assertEquals(enclaveInfoSVM.getEnclaveMaxThreadsNumber(), -1);

        // it's related to config file in test project.
        Enclave enclaveTEE = EnclaveFactory.create(EnclaveType.TEE_SDK);
        EnclaveInfo enclaveInfoTEE = enclaveTEE.getEnclaveInfo();
        assertEquals(enclaveInfoTEE.getEnclaveType(), EnclaveType.TEE_SDK);
        assertEquals(enclaveInfoTEE.isEnclaveDebuggable(), false);
        assertEquals(enclaveInfoTEE.getEnclaveEPCMemorySizeBytes(), 1500 * 1024 * 1024);
        assertEquals(enclaveInfoTEE.getEnclaveMaxThreadsNumber(), 50);

        // it's related to config file in test project.
        Enclave enclaveLIBOS = EnclaveFactory.create(EnclaveType.EMBEDDED_LIB_OS);
        EnclaveInfo enclaveInfoLIBOS = enclaveLIBOS.getEnclaveInfo();
        assertEquals(enclaveInfoLIBOS.getEnclaveType(), EnclaveType.EMBEDDED_LIB_OS);
        assertEquals(enclaveInfoLIBOS.isEnclaveDebuggable(), false);
        assertEquals(enclaveInfoLIBOS.getEnclaveEPCMemorySizeBytes(), 1500 * 1024 * 1024);
        assertEquals(enclaveInfoLIBOS.getEnclaveMaxThreadsNumber(), 50);

        enclaveInfoMXBeanStub = new ObjectName(DOMAIN_NAME + ":name=" + ENCLAVE_MX_BEAN_STUB);
        mxBeanService = ManagementFactory.getPlatformMBeanServer();
        mxBeanService.registerMBean(
                EnclaveInfoManager.getEnclaveInfoManagerInstance(),
                enclaveInfoMXBeanStub);

        registry = LocateRegistry.createRegistry(rmiPort);
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:" + rmiPort + "/" + DOMAIN_NAME);
        jmxConnector = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mxBeanService);
        jmxConnector.start();

        cl0.countDown();
        cl1.await();

        enclaveJVM.destroy();
        enclaveSVM.destroy();
        enclaveTEE.destroy();
        enclaveLIBOS.destroy();
    }

    @Test
    public void testEnclaveInfo() throws Exception {
        Thread serviceThread = new Thread(() -> {
            try {
                service();
            } catch (Exception e) {
                assert (false);
            }
        });
        serviceThread.setDaemon(true);
        serviceThread.start();
        // wait for mxbean service startup.
        cl0.await();
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:" + rmiPort + "/" + DOMAIN_NAME);
        JMXConnector jmxClient = JMXConnectorFactory.connect(url);
        MBeanServerConnection mbsClient = jmxClient.getMBeanServerConnection();
        ObjectName mBeanName = new ObjectName(DOMAIN_NAME + ":name=" + ENCLAVE_MX_BEAN_STUB);
        assertEquals(4, mbsClient.getAttribute(mBeanName, "EnclaveInstanceNumber"));
        CompositeData[] enclaveInfos = (CompositeData[]) mbsClient.getAttribute(mBeanName, "EnclaveInstancesInfo");
        assertEquals(4, enclaveInfos.length);
        for (CompositeData enclaveInfo : enclaveInfos) {
            String enclaveType = (String) enclaveInfo.get("enclaveType");
            switch (enclaveType) {
                case "MOCK_IN_JVM":
                case "MOCK_IN_SVM":
                    assertEquals((long)enclaveInfo.get("enclaveEPCMemorySizeBytes"), -1);
                    assertEquals((int)enclaveInfo.get("enclaveMaxThreadsNumber"), -1);
                    break;
                case "TEE_SDK":
                case "EMBEDDED_LIB_OS":
                    assertEquals((long)enclaveInfo.get("enclaveEPCMemorySizeBytes"), 1500 * 1024 * 1024);
                    assertEquals((int)enclaveInfo.get("enclaveMaxThreadsNumber"), 50);
                    break;
                case "NONE":
                    assert (false);
            }
        }
        // notify service exit.
        cl1.countDown();
    }
}
