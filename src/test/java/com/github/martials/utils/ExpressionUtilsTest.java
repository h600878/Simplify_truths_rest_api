package com.github.martials.utils;

import com.github.martials.exceptions.ExpressionInvalidException;
import com.github.martials.exceptions.MissingCharacterException;
import com.github.martials.exceptions.TooBigExpressionException;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ExpressionUtilsTest {

    private ExpressionUtils eu;

    @BeforeEach
    void setUp() {
        eu = new ExpressionUtils();
    }

    @ParameterizedTest
    @ValueSource(strings = {"A⋀B", "A⋁¬A", "A⋀B➔A", "A⋀¬A", "A⋀¬(A⋁B)", "¬¬A", "¬¬¬A", "Hello⋀World", "First⋀Second⋁Third"})
    void simplifyTest(String value) {

        try {
            eu.setSimplify(false);
            assertNotNull(eu.simplify(value));
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }

    }

    @ParameterizedTest
    @ValueSource(strings = {"A", "Hello", "Å", "Hello ⋀ World"})
    void isLegalExpressionTest(String value) {
        eu.setExpression(value);
        assertDoesNotThrow(() -> eu.isValid());
    }

    @ParameterizedTest
    @ValueSource(strings = {"#", "[", "⋁", "A B", "A⋁⋀", "A(", "A[", "A⋀()", "A¬B", "(A⋀B)(B⋀C)", "Hello World", "TEst a"})
    void throwsIllegalCharExceptionTest(String value) {
        eu.setExpression(value);
        assertThrows(ExpressionInvalidException.class, eu::isValid);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "(A", "A⋀(B"})
    void throwsMissingCharExceptionTest(String value) {
        eu.setExpression(value);
        assertThrows(MissingCharacterException.class, eu::isValid);
    }

    @Test
    void throwsTooBigExceptionTest() {
        eu.setExpression("A⋀B⋀C⋀D⋀E⋀F⋀G⋀H⋀I⋀J⋀K⋀L⋀M⋀N⋀O⋀P");
        assertThrows(TooBigExpressionException.class, eu::isValid);
    }

    @Test
    void notTooBigExpressionTest() {
        eu.setExpression("A⋀B⋀C⋀D⋀E⋀F⋀G⋀H⋀I⋀J");
        assertDoesNotThrow(() -> eu.isValid());
    }

}
