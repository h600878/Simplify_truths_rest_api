package com.github.martials.expressions;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;

import com.github.martials.enums.Operator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class OperatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"&", "⋀"})
    void getAndOperatorFromString(String operator) {
        assertEquals(Operator.AND, Operator.getOperator(operator));
    }

    @ParameterizedTest
    @ValueSource(strings = {":", "⋁"})
    void getOrOperatorFromString(String operator) {
        assertEquals(Operator.OR, Operator.getOperator(operator));
    }

    @ParameterizedTest
    @ValueSource(strings = {"!", "¬"})
    void getNotOperatorFromString(String operator) {
        assertEquals(Operator.NOT, Operator.getOperator(operator));
    }

    @ParameterizedTest
    @ValueSource(strings = {"->", "➔"})
    void getImplicationOperatorFromString(String operator) {
        assertEquals(Operator.IMPLICATION, Operator.getOperator(operator));
    }

    @ParameterizedTest
    @ValueSource(chars = {'&', '⋀'})
    void getOperatorFromChar(char operator) {
        assertEquals(Operator.AND, Operator.getOperator(operator));
    }

    @ParameterizedTest
    @ValueSource(chars = {':', '⋁'})
    void getOrOperatorFromChar(char operator) {
        assertEquals(Operator.OR, Operator.getOperator(operator));
    }

    @ParameterizedTest
    @ValueSource(chars = {'!', '¬'})
    void getNotOperatorFromChar(char operator) {
        assertEquals(Operator.NOT, Operator.getOperator(operator));
    }

    @ParameterizedTest
    @ValueSource(chars = {'➔'})
    void getImplicationOperatorFromChar(char operator) {
        assertEquals(Operator.IMPLICATION, Operator.getOperator(operator));
    }

    @ParameterizedTest
    @ValueSource(strings = {"&", ":", "!", "¬", "⋁", "⋀", "➔", "->"})
    void isOperatorString(String operator) {
        assertTrue(Operator.isOperator(operator), "Operator " + operator + " is not recognized");
    }

    @ParameterizedTest
    @ValueSource(chars = {'&', ':', '!', '¬', '⋁', '⋀', '➔'})
    void isOperatorString(char operator) {
        assertTrue(Operator.isOperator(operator), "Operator " + operator + " is not recognized");
    }

    @ParameterizedTest
    @ValueSource(strings = {"£", "T", "|"})
    void isNotOperatorString(String operator) {
        assertFalse(Operator.isOperator(operator), "Operator " + operator + " is recognized");
    }
}
