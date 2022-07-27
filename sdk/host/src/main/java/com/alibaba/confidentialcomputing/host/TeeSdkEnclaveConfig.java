package com.alibaba.confidentialcomputing.host;

import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;

import static com.alibaba.confidentialcomputing.host.ExtractLibrary.extractLibrary;

class TeeSdkEnclaveConfig {
    private final static String TEE_SDK_ENCLAVE_CONFIG_FILE = "tee_sdk_svm.conf";
    private long heapMaxSize;
    private int threadMaxNumber;

    private static String configFilePath;
    private static TeeSdkEnclaveConfig config;

    private TeeSdkEnclaveConfig(long heapMaxSize, int threadMaxNumber) {
        this.heapMaxSize = heapMaxSize;
        this.threadMaxNumber = threadMaxNumber;
    }

    private static long hexToDecimal(String value) {
        if (value.toLowerCase().startsWith("0x")) {
            return Long.parseLong(value.substring(2), 16);
        }
        return Long.parseLong(value);
    }

    private static String parseTeeSdkConfig(String path, String content) throws XPathExpressionException {
        InputSource source = new InputSource(new StringReader(content));
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        return xpath.evaluate(path, source);
    }

    static {
        try {
            configFilePath = extractLibrary(TeeSdkEnclave.class.getClassLoader(), TEE_SDK_ENCLAVE_CONFIG_FILE);
            File file = new File(configFilePath);
            String content = Files.readString(file.toPath(), Charset.forName("UTF-8"));
            String heapMaxSize = parseTeeSdkConfig("/EnclaveConfiguration/HeapMaxSize", content);
            String threadMaxSize = parseTeeSdkConfig("/EnclaveConfiguration/TCSNum", content);
            config = new TeeSdkEnclaveConfig(hexToDecimal(heapMaxSize), (int) hexToDecimal(threadMaxSize));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static TeeSdkEnclaveConfig getTeeSdkEnclaveConfigInstance() {
        return config;
    }

    long getHeapMaxSizeBytes() {
        return this.heapMaxSize;
    }

    int getThreadMaxNumber() {
        return this.threadMaxNumber;
    }
}
