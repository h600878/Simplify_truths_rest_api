package com.github.martials.utils;

import com.github.martials.exceptions.IllegalCharacterException;
import com.github.martials.exceptions.MissingCharaterException;
import com.github.martials.exceptions.TooBigExpressionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ExpressionUtilsTest {

    ExpressionUtils eu;

    @BeforeEach
    void setUp() {
        eu = new ExpressionUtils();
    }

    @Test
    void simplifyTest() {

        try {
            eu.setSimplify(false);
            Assertions.assertNotNull(eu.simplify("A⋀B"));
            Assertions.assertNotNull(eu.simplify("A⋁¬A"));
            Assertions.assertNotNull(eu.simplify("A⋀B➔A"));
            Assertions.assertNotNull(eu.simplify("A⋀¬A"));
            Assertions.assertNotNull(eu.simplify("A⋀¬(A⋁B)"));
            Assertions.assertNotNull(eu.simplify("¬¬A"));
            Assertions.assertNotNull(eu.simplify("¬¬¬A"));
        }
        catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }

    }

    @ParameterizedTest
    @ValueSource(strings = {"A", "[Hello]", "Å", "[Hello]⋀[World]"})
    void isLegalExpressionTest(String value) {
        eu.setExpression(value);
        Assertions.assertDoesNotThrow(eu::isLegalExpression);
    }

    @ParameterizedTest
    @ValueSource(strings = {"#", "⋁", "AB", "A⋁⋀", "A(", "A[", "A⋀(B]", "A⋀()", "A¬B", "(A⋀B)(B⋀C)", "[Hello][World]"})
    void throwsIllegalCharExpressionTest(String value) {
        eu.setExpression(value);
        Assertions.assertThrows(IllegalCharacterException.class, eu::isLegalExpression);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "[A", "(A"})
    void throwsMissingCharExpressionTest(String value) {
        eu.setExpression(value);
        Assertions.assertThrows(MissingCharaterException.class, eu::isLegalExpression);
    }

    @Test
    void throwsTooBigExpressionTest() {
        eu.setExpression("A⋀B⋀C⋀D⋀E⋀F⋀G⋀H⋀I⋀J⋀K");
        Assertions.assertThrows(TooBigExpressionException.class, eu::isLegalExpression);
    }

}
