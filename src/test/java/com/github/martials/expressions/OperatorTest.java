package com.github.martials.expressions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OperatorTest {


    @BeforeEach
    void setUp() {
    }

    @Test
    void getOperator() {
        Assertions.assertEquals(Operator.and, Operator.getOperator("&"));
        Assertions.assertEquals(Operator.and, Operator.getOperator('&'));

        Assertions.assertEquals(Operator.or, Operator.getOperator('|'));

        Assertions.assertEquals(Operator.implication, Operator.getOperator("->"));

    }

    @Test
    void isOperator() {
        Assertions.assertTrue(Operator.isOperator('¬'));
        Assertions.assertTrue(Operator.isOperator("¬"));
        Assertions.assertTrue(Operator.isOperator('&'));
        Assertions.assertTrue(Operator.isOperator("&"));

        for (Operator ops : Operator.getPredefined()) {
            for (String s : ops.getValues()) {
                Assertions.assertTrue(Operator.isOperator(s));
            }
        }

        Assertions.assertFalse(Operator.isOperator("£"));
        Assertions.assertFalse(Operator.isOperator('T'));
    }

    @Test
    void testEquals() {
        Assertions.assertNotEquals(Operator.or, Operator.not);
        Assertions.assertNotEquals(Operator.and, Operator.or);
        Assertions.assertNotEquals(Operator.implication, Operator.and);

        Assertions.assertEquals(Operator.or, Operator.or);
    }
}
