package com.github.martials.utils;

import com.github.martials.exceptions.IllegalCharacterException;
import com.github.martials.exceptions.MissingCharaterException;
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
        Assertions.assertDoesNotThrow(eu::isLegalExpression);
        eu.setExpression("[Hello]");
        Assertions.assertDoesNotThrow(eu::isLegalExpression);
        eu.setExpression("Å");
        Assertions.assertDoesNotThrow(eu::isLegalExpression);
        eu.setExpression("[Hello]⋀[World]");
        Assertions.assertDoesNotThrow(eu::isLegalExpression);

    }

    @Test
    void isIllegalExpressionTest() {

        eu.setExpression("");
        Assertions.assertThrows(MissingCharaterException.class, eu::isLegalExpression);
        eu.setExpression("#");
        Assertions.assertThrows(IllegalCharacterException.class, eu::isLegalExpression);
        eu.setExpression("⋁");
        Assertions.assertThrows(IllegalCharacterException.class, eu::isLegalExpression);
        eu.setExpression("AB");
        Assertions.assertThrows(IllegalCharacterException.class, eu::isLegalExpression);
        eu.setExpression("A⋁⋀");
        Assertions.assertThrows(IllegalCharacterException.class, eu::isLegalExpression);
        eu.setExpression("A(");
        Assertions.assertThrows(IllegalCharacterException.class, eu::isLegalExpression);
        eu.setExpression("A[");
        Assertions.assertThrows(IllegalCharacterException.class, eu::isLegalExpression);
        eu.setExpression("[A");
        Assertions.assertThrows(MissingCharaterException.class, eu::isLegalExpression);
        eu.setExpression("A⋀(B]");
        Assertions.assertThrows(IllegalCharacterException.class, eu::isLegalExpression);
        eu.setExpression("A⋀()");
        Assertions.assertThrows(IllegalCharacterException.class, eu::isLegalExpression);
        eu.setExpression("A¬B");
        Assertions.assertThrows(IllegalCharacterException.class, eu::isLegalExpression);
        eu.setExpression("(A⋀B)(B⋀C)");
        Assertions.assertThrows(IllegalCharacterException.class, eu::isLegalExpression);
        eu.setExpression("[Hello][World]");
        Assertions.assertThrows(IllegalCharacterException.class, eu::isLegalExpression);
    }

}
