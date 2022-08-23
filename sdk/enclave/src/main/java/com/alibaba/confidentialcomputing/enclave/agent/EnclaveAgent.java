package com.alibaba.confidentialcomputing.enclave.agent;

import com.alibaba.confidentialcomputing.common.SerializationHelper;
import com.alibaba.confidentialcomputing.common.SocketEnclaveInvocationContext;
import com.alibaba.confidentialcomputing.common.exception.ConfidentialComputingException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.Executors;

class EnclaveAgent {
    private static final String HTTP_EXECUTE_THREAD_NAME = "enclave_http_remote_invoking_thread";
    private static final String HTTP_SERVER_NAME = "/enclaveAgent";
    private static final EnclaveAgentServiceImpl service = new EnclaveAgentServiceImpl();
    private static volatile HttpServer httpServer = null;

    // socket service port is from host side.
    public static void main(String[] args) throws ConfidentialComputingException, IOException {
        if (args.length != 3) {
            throw new ConfidentialComputingException("lib os enclave agent service's port resource is not available.");
        }
        int portHost = Integer.parseInt(args[0]);
        int portEnclave = Integer.parseInt(args[1]);
        int httpThreadPoolSize = Integer.parseInt(args[2]);

        notifyHostAndCreateHttpConnect(portHost, portEnclave, httpThreadPoolSize);

        // wait for enclave shut down notification.
        EnclaveShutDown.shutDownWait();
    }

    private static void notifyHostAndCreateHttpConnect(int portHost, int portEnclave, int threadPoolSize) throws IOException {
        // create http connection and wait for request from host.
        httpServer = HttpServer.create(new InetSocketAddress(portEnclave), 0);
        httpServer.createContext(HTTP_SERVER_NAME, new EnclaveHttpHandler());
        httpServer.setExecutor(Executors.newScheduledThreadPool(threadPoolSize, r -> {
            Thread thread = new Thread(r);
            thread.setName(HTTP_EXECUTE_THREAD_NAME);
            thread.setDaemon(true);
            return thread;
        }));
        httpServer.start();
        // notify host that enclave jvm had started up.
        new Socket("localhost", portHost);
    }

    static void closeHttpService() {
        httpServer.stop(0);
    }

    private static void writeBackResponse(HttpExchange exchange, byte[] response) throws IOException {
        exchange.sendResponseHeaders(200, response.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(response);
        outputStream.flush();
        outputStream.close();
    }

    static class EnclaveHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            InputStream inputStream = exchange.getRequestBody();
            byte[] payload = inputStream.readAllBytes();
            inputStream.close();
            SocketEnclaveInvocationContext context = null;

            try {
                context = (SocketEnclaveInvocationContext) SerializationHelper.deserialize(payload);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            switch (Objects.requireNonNull(context).getAgentServiceName()) {
                case SocketEnclaveInvocationContext.SERVICE_LOADING:
                    writeBackResponse(exchange, service.loadService(context.getServiceHandler().getServiceInterfaceName()));
                    break;
                case SocketEnclaveInvocationContext.SERVICE_UNLOADING:
                    writeBackResponse(exchange, service.unloadService(context.getServiceHandler()));
                    break;
                case SocketEnclaveInvocationContext.METHOD_INVOCATION:
                    writeBackResponse(exchange, service.invokeMethod(context));
                    break;
                case SocketEnclaveInvocationContext.REMOTE_ATTESTATION_GENERATE:
                    writeBackResponse(exchange, service.generateAttestationReport(context.getUserData()));
                    break;
                case SocketEnclaveInvocationContext.ENCLAVE_DESTROY:
                    writeBackResponse(exchange, service.destroy());
                    break;
                default:
            }
        }
    }
}
