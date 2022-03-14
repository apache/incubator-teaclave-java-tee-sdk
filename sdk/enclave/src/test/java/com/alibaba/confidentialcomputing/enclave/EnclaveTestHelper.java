package com.alibaba.confidentialcomputing.enclave;

import com.alibaba.confidentialcomputing.enclave.testservice.MathService;
import com.alibaba.confidentialcomputing.enclave.testservice.NumericMath;

public class EnclaveTestHelper {
    public static final String MATH_SERVICE = MathService.class.getName();
    public static final String NUMERIC_MATH = NumericMath.class.getName();
    public static final String[] MATH_ADD_PARAM_TYPES = {"java.lang.Number", "java.lang.Number"};
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
}
