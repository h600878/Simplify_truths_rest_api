package com.github.martials.expressions;

import com.github.martials.utils.ExpressionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExpressionTest {

    Expression alwaysTrue1, alwaysTrue2, alwaysFalse1, alwaysFalse2;

    @BeforeEach
    void setup() {
        alwaysTrue1 = ExpressionUtils.simplify("A⋁¬A", false);
        alwaysTrue2 = ExpressionUtils.simplify("A⋀B➔A", false);
        alwaysFalse1 = ExpressionUtils.simplify("A⋀¬A", false);
        alwaysFalse2 = ExpressionUtils.simplify("A⋀¬(A⋁B)", false);
    }

    @Test
    void testEquals() {

        Assertions.assertNotEquals(alwaysTrue1, alwaysTrue2);
        Assertions.assertNotEquals(alwaysFalse1, alwaysFalse2);

        Assertions.assertEquals(alwaysFalse1, alwaysFalse1);

    }

    @Test
    void equalsAndOpposite() {

        Assertions.assertFalse(alwaysTrue1.equalsAndOpposite(alwaysTrue2));
        Assertions.assertFalse(alwaysFalse1.equalsAndOpposite(alwaysFalse1));

        Assertions.assertTrue(alwaysTrue1.equalsAndOpposite(alwaysFalse1));

    }

    @Test
    void removeParenthesis() {
    }

    @Test
    void distributiveProperty() {
    }

    @Test
    void deMorgansLaws() {
    }

    @Test
    void isInverse() {
    }

    @Test
    void associativeProperty() {
    }

    @Test
    void commutativeProperty() {
        Assertions.fail();
    }

    @Test
    void eliminationOfImplication() {
    }

    @Test
    void absorptionLaw() {
    }

    @Test
    void doubleNegation() {
    }

    @Test
    void getNumberOfAtomics() {
    }

    @Test
    void solve() {
    }

    @Test
    void isAtomic() {
    }

    @Test
    void testToString() {
    }
}
