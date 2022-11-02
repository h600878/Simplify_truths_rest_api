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
        Assertions.fail();
    }

    @Test
    void isOperator() {
        Assertions.assertTrue(Operator.isOperator('¬'));
        Assertions.assertTrue(Operator.isOperator("¬"));
        Assertions.assertTrue(Operator.isOperator('&'));
        Assertions.assertTrue(Operator.isOperator("&"));
        // TODO
    }

    @Test
    void testEquals() {
        Assertions.fail();
    }
}
