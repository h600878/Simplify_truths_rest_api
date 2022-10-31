package com.github.martials.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.martials.utils.ExpressionUtils.*;

public class ExpressionUtilsTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void simplifyTest() {

        try {
            Assertions.assertNotNull(simplify("A⋀B", false));
            Assertions.assertNotNull(simplify("A⋁¬A", false));
            Assertions.assertNotNull(simplify("A⋀B➔A", false));
            Assertions.assertNotNull(simplify("A⋀¬A", false));
            Assertions.assertNotNull(simplify("A⋀¬(A⋁B)", false));
            Assertions.assertNotNull(ExpressionUtils.simplify("¬¬A", false));
            Assertions.assertNotNull(ExpressionUtils.simplify("¬¬¬A", false));
        }
        catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }

    }

    @Test
    void isLegalExpressionTest() {

        Assertions.assertEquals("", isLegalExpression("A"));
        Assertions.assertEquals("", isLegalExpression("[Hello]"));
        Assertions.assertEquals("", isLegalExpression("Å"));
        Assertions.assertEquals("", isLegalExpression("[Hello]⋀[World]"));

    }

    @Test
    void isIllegalExpressionTest() {

        Assertions.assertNotEquals("", isLegalExpression(""));
        Assertions.assertNotEquals("", isLegalExpression("#"));
        Assertions.assertNotEquals("", isLegalExpression("⋁"));
        Assertions.assertNotEquals("", isLegalExpression("AB"));
        Assertions.assertNotEquals("", isLegalExpression("A⋁⋀"));
        Assertions.assertNotEquals("", isLegalExpression("A("));
        Assertions.assertNotEquals("", isLegalExpression("A["));
        Assertions.assertNotEquals("", isLegalExpression("[A"));
        Assertions.assertNotEquals("", isLegalExpression("A⋀(B]"));
        Assertions.assertNotEquals("", isLegalExpression("A⋀()"));
        Assertions.assertNotEquals("", isLegalExpression("A¬B"));
        Assertions.assertNotEquals("", isLegalExpression("(A⋀B)(B⋀C)"));
        Assertions.assertNotEquals("", isLegalExpression("[Hello][World]"));
    }
}
