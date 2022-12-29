package com.github.martials.expressions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.martials.enums.Operator;

public class OperatorTest {


    @BeforeEach
    void setUp() {
    }

    @Test
    void getOperator() {
        Assertions.assertEquals(Operator.AND, Operator.getOperator("&"));
        Assertions.assertEquals(Operator.AND, Operator.getOperator('&'));

        Assertions.assertEquals(Operator.OR, Operator.getOperator('|'));

        Assertions.assertEquals(Operator.IMPLICATION, Operator.getOperator("->"));

    }

    @Test
    void isOperator() {
        Assertions.assertTrue(Operator.isOperator('¬'));
        Assertions.assertTrue(Operator.isOperator("¬"));
        Assertions.assertTrue(Operator.isOperator('&'));
        Assertions.assertTrue(Operator.isOperator("&"));

        for (Operator ops : Operator.values()) {
            for (String s : ops.getValues()) {
                Assertions.assertTrue(Operator.isOperator(s));
            }
        }

        Assertions.assertFalse(Operator.isOperator("£"));
        Assertions.assertFalse(Operator.isOperator('T'));
    }

    @Test
    void testEquals() {
        Assertions.assertNotEquals(Operator.OR, Operator.NOT);
        Assertions.assertNotEquals(Operator.AND, Operator.OR);
        Assertions.assertNotEquals(Operator.IMPLICATION, Operator.AND);

        Assertions.assertEquals(Operator.OR, Operator.OR);
    }
}
