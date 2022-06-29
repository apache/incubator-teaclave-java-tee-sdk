package com.alibaba.confidentialcomputing.enclave;

import com.alibaba.confidentialcomputing.enclave.testservice.IntegerMath;
import com.alibaba.confidentialcomputing.enclave.testservice.MathService;
import com.alibaba.confidentialcomputing.enclave.testservice.NumericMath;
import com.alibaba.confidentialcomputing.enclave.testservice.Point;
import com.alibaba.confidentialcomputing.enclave.testservice.PointMath;
import com.oracle.svm.core.annotate.AutomaticFeature;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeSerialization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import static com.alibaba.confidentialcomputing.enclave.EnclaveTestHelper.EMPTY_OBJECT_ARRAY;
import static com.alibaba.confidentialcomputing.enclave.EnclaveTestHelper.EMPTY_STRING_ARRAY;
import static com.alibaba.confidentialcomputing.enclave.EnclaveTestHelper.INTEGER_MATH;
import static com.alibaba.confidentialcomputing.enclave.EnclaveTestHelper.MATH_ADD_PARAM_TYPES;
import static com.alibaba.confidentialcomputing.enclave.EnclaveTestHelper.MATH_SERVICE;
import static com.alibaba.confidentialcomputing.enclave.EnclaveTestHelper.NUMERIC_MATH;
import static com.alibaba.confidentialcomputing.enclave.EnclaveTestHelper.POINT_MATH;
import static com.alibaba.confidentialcomputing.enclave.EnclaveTestHelper.POINT_MATH_ADD_PARAM_TYPES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class RunWithNativeImageTest {

    @TestTarget(RunWithNativeImageTest.class)
    static
    class SimpleRunPreparation extends NativeImageTest {

        @Override
        public SVMCompileElements specifyTestClasses() {
            SVMCompileElements ret = new SVMCompileElements();
            // Specify the service file
            ret.addServices("META-INF/services/com.alibaba.confidentialcomputing.enclave.testservice.MathService");

            // Specify the classes need to be statically compiled into native image for this test
            ret.addClasses(
                    UTFeature.class, MathService.class, NumericMath.class, PointMath.class, Point.class, IntegerMath.class
            );
            return ret;
        }
    }

    static private NativeImageTest nativeImageTest;

    @BeforeAll
    public static void prepareLibraries() {
        nativeImageTest = new SimpleRunPreparation();
        nativeImageTest.prepareNativeLibraries();
    }

    @BeforeEach
    public void setup() {
        EnclaveTestHelper.createIsolate();
    }

    @AfterEach
    public void teardown() {
        EnclaveTestHelper.destroyIsolate();
    }

    /**
     * Test calling a method with primitive parameters and returned value.
     */
    @Test
    public void testSimpleRun() {
        String identity = loadAndGetService(NUMERIC_MATH);
        assertEquals(3, callIntAdd(identity, 1, 2));
    }

    /**
     * Test a multithreading case.
     */
    @Test
    public void testMultiThreadRun() {
        String identity1 = loadAndGetService(NUMERIC_MATH);
        String identity2 = loadAndGetService(NUMERIC_MATH);

        Thread t1 = new Thread(() -> {
            try {
                assertEquals(3, callIntAdd(identity1, 1, 2));
            } catch (Exception e) {
                fail(e);
            }
        });
        Thread t2 = new Thread(() -> {
            try {
                assertEquals(9, callIntAdd(identity1, 4, 5));
            } catch (Exception e) {
                fail(e);
            }
        });
        Thread t3 = new Thread(() -> {
            try {
                assertEquals(9, callIntAdd(identity2, 4, 5));
            } catch (Exception e) {
                fail(e);
            }
        });
        t1.start();
        t2.start();
        t3.start();
        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            fail(e);
        }
        assertEquals(2, callGetCounter(identity1));
        assertEquals(1, callGetCounter(identity2));
    }

    /**
     * Test calling an interface default method.
     */
    @Test
    public void testServiceDefaultMethod() {
        String identity = loadAndGetService(INTEGER_MATH);
        int ret = (Integer) call(identity, INTEGER_MATH, "getConstant", EMPTY_STRING_ARRAY, EMPTY_OBJECT_ARRAY);
        assertEquals(100, ret);
    }

    /**
     * Test calling a method with referenced type of parameters and returned values.
     */
    @Test
    public void testPointAdd() {
        String id = loadAndGetService(POINT_MATH);
        Point ret = (Point) call(id, POINT_MATH, "add", POINT_MATH_ADD_PARAM_TYPES,
                new Object[]{new Point(1, 1), new Point(2, 2)});
        assertEquals(3, ret.x);
        assertEquals(3, ret.y);
    }

    /**
     *  Test the {@code getRandomNumber} method actually calls the native method {@code int get_native_random_number(void* data, long size)}
     *  in {@code enc_invoke_entry_test.c} file. It doesn't return a random value, but a fixed byte array.
     */
    @Test
    public void testCallNativeGetRandomNumber() {
        String identity = loadAndGetService(NUMERIC_MATH);
        int size = 32;
        byte[] ret = (byte[]) call(identity, NUMERIC_MATH, "getRandomNumber", new String[]{"int"}, new Object[]{size});
        assertNotNull(ret);
        for (int i = 0; i < size; i++) {
            assertEquals(ret[i], i % 256);
        }
    }

    private static String loadAndGetService(String implementation) {
        return EnclaveTestHelper.loadAndGetService(MATH_SERVICE, implementation, 3);
    }

    private static int callIntAdd(String id, int x, int y) {
        return (Integer) call(id, NUMERIC_MATH, "add", MATH_ADD_PARAM_TYPES, new Object[]{x, y});
    }

    private static int callGetCounter(String id) {
        return (Integer) call(id, NUMERIC_MATH, "getCounter", EMPTY_STRING_ARRAY, EMPTY_OBJECT_ARRAY);
    }

    private static Object call(String id, String className, String methodName, String[] paramTypes, Object[] paramValues) {
        return EnclaveTestHelper.call(id, MATH_SERVICE, className, methodName, paramTypes, paramValues);
    }

    @AutomaticFeature
    static class UTFeature implements Feature {

        @Override
        public void beforeAnalysis(BeforeAnalysisAccess access) {
            RuntimeSerialization.register(Number.class, NoSuchAlgorithmException.class);
        }
    }
}
