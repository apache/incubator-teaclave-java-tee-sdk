package com.alibaba.confidentialcomputing.enclave.framework;

import com.alibaba.confidentialcomputing.common.EnclaveInvocationContext;
import com.alibaba.confidentialcomputing.common.EnclaveInvocationResult;
import com.alibaba.confidentialcomputing.common.ServiceHandler;
import com.alibaba.confidentialcomputing.common.exception.ConfidentialComputingException;
import com.alibaba.confidentialcomputing.enclave.testservice.MathService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.alibaba.confidentialcomputing.enclave.EnclaveTestHelper.EMPTY_OBJECT_ARRAY;
import static com.alibaba.confidentialcomputing.enclave.EnclaveTestHelper.EMPTY_STRING_ARRAY;
import static com.alibaba.confidentialcomputing.enclave.EnclaveTestHelper.MATH_ADD_PARAM_TYPES;
import static com.alibaba.confidentialcomputing.enclave.EnclaveTestHelper.MATH_SERVICE;
import static com.alibaba.confidentialcomputing.enclave.EnclaveTestHelper.NUMERIC_MATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;

public class ServiceMethodInvokerTest {

    private static ServiceMethodInvoker serviceMethodInvoker = new ServiceMethodInvoker();
    private ServiceHandler[] services;

    @BeforeEach
    public void setup() {
        services = EnclaveContext.getInstance().loadService(MathService.class);
        assertEquals(3, services.length);
        assertEquals(MATH_SERVICE, services[0].getServiceInterfaceName());
        assertEquals(NUMERIC_MATH, services[0].getServiceImplClassName());
    }

    @AfterEach
    public void tear() {
        EnclaveContext.getInstance().clearCache();
    }

    /**
     * Test the invocation is successfully made.
     */
    @Test
    public void testSuccessCall() {
        EnclaveInvocationResult result = callNumericAdd(services[0], 1, 2);
        assertNotNull(result);
        Object wrappedResult = result.getResult();
        assertNotNull(wrappedResult, "Expect to have non-null result from invoking service method call.");
        assertNull(result.getException());
        assertEquals(3, (Integer) wrappedResult);
    }

    /**
     * Test the exception thrown from service implementation is properly returned.
     */
    @Test
    public void testInvocationFail() {
        // Prepare a div(1, 0) method call which should report a divide 0 exception.
        EnclaveInvocationResult result = callServiceImplMethod(services[0],
                "div", MATH_ADD_PARAM_TYPES, new Object[]{1, 0});
        assertNotNull(result);
        Object wrappedResult = result.getResult();
        assertNull(wrappedResult, "Expect to have non-null result from invoking service method call.");
        Throwable e = result.getException();
        assertNotNull(e);
        assertTrue(e instanceof InvocationTargetException);
        assertTrue(((InvocationTargetException) e).getCause() instanceof ArithmeticException);
    }

    /**
     * Call a not exist service
     */
    @Test
    public void testCallNotExistService() {
        EnclaveInvocationResult ret = callServiceImplMethod(new ServiceHandler("MATH_SERVICE", services[0].getServiceImplClassName(), services[0].getInstanceIdentity()),
                "add",
                MATH_ADD_PARAM_TYPES,
                new Object[]{1, 2});
        assertNotNull(ret);
        assertNull(ret.getResult());
        assertTrue(ret.getException() instanceof ConfidentialComputingException);
    }

    /**
     * Call a not exist service implementation
     */
    @Test
    public void testCallNotExistImpl() {
        EnclaveInvocationResult ret = callServiceImplMethod(
                new ServiceHandler(services[0].getServiceInterfaceName(), "NUMERIC_MATH", services[0].getInstanceIdentity()),
                "add",
                MATH_ADD_PARAM_TYPES,
                new Object[]{1, 2});
        assertNotNull(ret);
        assertNull(ret.getResult());
        assertTrue(ret.getException() instanceof ConfidentialComputingException);
    }

    /**
     * Call a not exist service implementation method
     */
    @Test
    public void testCallNotExistMethod() {
        EnclaveInvocationResult ret = callServiceImplMethod(services[0],
                "add123",
                MATH_ADD_PARAM_TYPES,
                new Object[]{1, 2});
        assertNotNull(ret);
        assertNull(ret.getResult());
        assertTrue(ret.getException() instanceof ConfidentialComputingException);
        assertTrue(ret.getException().getCause() instanceof NoSuchMethodException);
    }

    @Test
    public void testServiceConsistency() {
        ServiceHandler[] secondLoadings = EnclaveContext.getInstance().loadService(MathService.class);
        int i = 0;
        while (i++ < 2) {
            callNumericAdd(services[0], 1, 2);
        }
        callNumericAdd(secondLoadings[0], 1, 2);
        assertEquals(2, callGetCounter(services[0]).getResult(),
                "Add method in service instance with identity " + services[0].getInstanceIdentity() + "has been called twice, the counter should be 2");
        assertEquals(1, callGetCounter(secondLoadings[0]).getResult(),
                "Add method in service instance with identity " + secondLoadings[0].getInstanceIdentity() + "has been called twice, the counter should be 1");
    }

    @Test
    public void testDefaultMethod() {
        EnclaveInvocationResult result = callServiceImplMethod(services[0],
                "getConstant",
                EMPTY_STRING_ARRAY,
                EMPTY_OBJECT_ARRAY);
        assertNotNull(result);
        Object wrappedResult = result.getResult();
        assertNotNull(wrappedResult, "Expect to have non-null result from invoking service method call.");
        assertNull(result.getException());
        assertEquals(100, (Integer) wrappedResult);
    }

    @Test
    public void testGrandChildMethod() {
        EnclaveInvocationResult result = callNumericAdd(services[2], 1, 2);
        assertNotNull(result);
        Object wrappedResult = result.getResult();
        assertNotNull(wrappedResult, "Expect to have non-null result from invoking service method call.");
        assertNull(result.getException());
        assertEquals(3, (Integer) wrappedResult);
    }


    private static EnclaveInvocationResult callGetCounter(ServiceHandler serviceHandler) {
        return callServiceImplMethod(serviceHandler,
                "getCounter",
                EMPTY_STRING_ARRAY, EMPTY_OBJECT_ARRAY);
    }

    private static EnclaveInvocationResult callNumericAdd(ServiceHandler serviceHandler, int x, int y) {
        return callServiceImplMethod(serviceHandler,
                "add",
                MATH_ADD_PARAM_TYPES,
                new Object[]{x, y});
    }

    private static EnclaveInvocationResult callServiceImplMethod(ServiceHandler serviceHandler, String method,
                                                                 String[] paramTypes, Object[] paramValues) {
        EnclaveInvocationContext input = new EnclaveInvocationContext(serviceHandler,
                method, paramTypes, paramValues);
        return serviceMethodInvoker.callMethod(input);
    }
}
