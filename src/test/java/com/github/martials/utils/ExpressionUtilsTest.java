package com.github.martials.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @Test
    void isLegalExpressionTest() {

        eu.setExpression("A");
        Assertions.assertEquals("", eu.isLegalExpression());
        eu.setExpression("[Hello]");
        Assertions.assertEquals("", eu.isLegalExpression());
        eu.setExpression("Å");
        Assertions.assertEquals("", eu.isLegalExpression());
        eu.setExpression("[Hello]⋀[World]");
        Assertions.assertEquals("", eu.isLegalExpression());

    }

    @Test
    void isIllegalExpressionTest() {

        eu.setExpression("");
        Assertions.assertNotEquals("", eu.isLegalExpression());
        eu.setExpression("#");
        Assertions.assertNotEquals("", eu.isLegalExpression());
        eu.setExpression("⋁");
        Assertions.assertNotEquals("", eu.isLegalExpression());
        eu.setExpression("AB");
        Assertions.assertNotEquals("", eu.isLegalExpression());
        eu.setExpression("A⋁⋀");
        Assertions.assertNotEquals("", eu.isLegalExpression());
        eu.setExpression("A(");
        Assertions.assertNotEquals("", eu.isLegalExpression());
        eu.setExpression("A[");
        Assertions.assertNotEquals("", eu.isLegalExpression());
        eu.setExpression("[A");
        Assertions.assertNotEquals("", eu.isLegalExpression());
        eu.setExpression("A⋀(B]");
        Assertions.assertNotEquals("", eu.isLegalExpression());
        eu.setExpression("A⋀()");
        Assertions.assertNotEquals("", eu.isLegalExpression());
        eu.setExpression("A¬B");
        Assertions.assertNotEquals("", eu.isLegalExpression());
        eu.setExpression("(A⋀B)(B⋀C)");
        Assertions.assertNotEquals("", eu.isLegalExpression());
        eu.setExpression("[Hello][World]");
        Assertions.assertNotEquals("", eu.isLegalExpression());
    }
}
