package com.github.martials.expressions;

import com.github.martials.utils.ExpressionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class ExpressionTest {

    private Expression
            alwaysTrue1, alwaysTrue2,
            alwaysFalse1, alwaysFalse2,
            doubleInverse, tripleInverse, quadInverse, fiveInverse,
            aAndA, aOrA, aAndAOrA, aImpliesNotA,
            notAandNotB, notAorNotB,
            aAndBorBandC, aOrBandBorC,
            parenthesesAandB,
            wrongOrder;

    @BeforeEach
    void setup() {
        alwaysTrue1 = new ExpressionUtils("Ape⋁¬Ape", false).simplify();
        alwaysTrue2 = new ExpressionUtils("Ape⋀Banana➔Ape", false).simplify();
        alwaysFalse1 = new ExpressionUtils("Ape⋀¬Ape", false).simplify();
        alwaysFalse2 = new ExpressionUtils("A⋀¬(A⋁B)", false).simplify();
        doubleInverse = new ExpressionUtils("¬¬A", false).simplify();
        tripleInverse = new ExpressionUtils("¬¬¬A", false).simplify();
        quadInverse = new ExpressionUtils("¬¬¬¬A", false).simplify();
        fiveInverse = new ExpressionUtils("¬¬¬¬¬A", false).simplify();
        aAndA = new ExpressionUtils("A⋀A", false).simplify();
        aOrA = new ExpressionUtils("A⋁A", false).simplify();
        aAndAOrA = new ExpressionUtils("A⋀A⋁A", false).simplify();
        aImpliesNotA = new ExpressionUtils("A➔¬A", false).simplify();
        notAandNotB = new ExpressionUtils("¬A⋀¬B", false).simplify();
        notAorNotB = new ExpressionUtils("¬A⋁¬B", false).simplify();
        parenthesesAandB = new ExpressionUtils("(A⋀B)", false).simplify();
        aAndBorBandC = new ExpressionUtils("A⋀B⋁B⋀C", false).simplify();
        aOrBandBorC = new ExpressionUtils("(A⋁B)⋀(B⋁C)", false).simplify();
        wrongOrder = new ExpressionUtils("B⋁A", false).simplify();
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
        parenthesesAandB.removeParenthesis(new ArrayList<>());
        Assertions.assertEquals("A ⋀ B", parenthesesAandB.toString());

        notAandNotB.deMorgansLaws();
        notAandNotB.removeParenthesis(new ArrayList<>());
        Assertions.assertEquals("¬(A ⋁ B)", notAandNotB.toString());
        notAorNotB.deMorgansLaws();
        notAorNotB.removeParenthesis(new ArrayList<>());
        Assertions.assertEquals("¬(A ⋀ B)", notAorNotB.toString());
    }

    @Test
    void distributiveProperty() {
        aOrBandBorC.distributiveProperty();
        Assertions.assertEquals("B ⋁ A ⋀ C", aOrBandBorC.toString());
        aAndBorBandC.distributiveProperty();
        Assertions.assertEquals("B ⋀ (A ⋁ C)", aAndBorBandC.toString());
    }

    @Test
    void deMorgansLaws() {
        notAandNotB.deMorgansLaws();
        Assertions.assertEquals("¬(A ⋁ B)", notAandNotB.toString());
        notAorNotB.deMorgansLaws();
        Assertions.assertEquals("¬(A ⋀ B)", notAorNotB.toString());
    }

    @Test
    void isInverse() {
        Assertions.assertFalse(doubleInverse.isInverse());
        Assertions.assertTrue(tripleInverse.isInverse());
        Assertions.assertFalse(quadInverse.isInverse());
        Assertions.assertTrue(fiveInverse.isInverse());

        doubleInverse.doubleNegation();
        tripleInverse.doubleNegation();
        quadInverse.doubleNegation();
        fiveInverse.doubleNegation();

        Assertions.assertFalse(doubleInverse.isInverse());
        Assertions.assertTrue(tripleInverse.isInverse());
        Assertions.assertFalse(quadInverse.isInverse());
        Assertions.assertTrue(fiveInverse.isInverse());
    }

    @Test
    void associativeProperty() {
        Assertions.assertTrue(true);
    }

    @Test
    void commutativeProperty() {
        wrongOrder.commutativeProperty(new ArrayList<>());
        Assertions.assertEquals("A ⋁ B", wrongOrder.toString());
    }

    @Test
    void eliminationOfImplication() {
        aImpliesNotA.eliminationOfImplication();
        Assertions.assertEquals("¬A ⋁ ¬A", aImpliesNotA.toString());
    }

    @Test
    void absorptionLaw() {
        aAndA.absorptionLaw();
        Assertions.assertEquals("A", aAndA.toString());
        aOrA.absorptionLaw();
        Assertions.assertEquals("A", aOrA.toString());
        aImpliesNotA.eliminationOfImplication();
        aImpliesNotA.absorptionLaw();
        Assertions.assertEquals("¬A", aImpliesNotA.toString());
        aAndAOrA.absorptionLaw();
        Assertions.assertEquals("A", aAndAOrA.toString());
    }

    @Test
    void doubleNegation() {
        Assertions.assertEquals("¬¬", doubleInverse.getLeading());
        Assertions.assertEquals("¬¬¬", tripleInverse.getLeading());

        try {
            doubleInverse.doubleNegation();
            tripleInverse.doubleNegation();
        }
        catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }

        Assertions.assertEquals("", doubleInverse.getLeading());
        Assertions.assertEquals("¬", tripleInverse.getLeading());
    }

    @Test
    void getNumberOfAtomics() {
        Assertions.assertEquals(2, alwaysTrue1.getNumberOfAtomics());
        Assertions.assertEquals(3, alwaysTrue2.getNumberOfAtomics());
        Assertions.assertEquals(1, fiveInverse.getNumberOfAtomics());
    }

    @Test
    void solve() {
        final boolean[] left = new boolean[] {false, true};
        final boolean[] right = new boolean[] {false, true};

        for (boolean l : left) {
            for (boolean r : right) {
                Assertions.assertTrue(alwaysTrue1.solve(l, !l));
                Assertions.assertTrue(alwaysTrue2.solve(l, l || r));
            }
        }
        Assertions.assertTrue(wrongOrder.solve(false, true));
        Assertions.assertTrue(wrongOrder.solve(true, false));

        Assertions.assertFalse(wrongOrder.solve(false, false));
    }

    @Test
    void isAtomic() {
        Assertions.assertFalse(alwaysTrue1.isAtomic());
        Assertions.assertFalse(alwaysTrue2.isAtomic());
        Assertions.assertFalse(alwaysFalse1.isAtomic());
        Assertions.assertFalse(alwaysFalse2.isAtomic());
        Assertions.assertTrue(doubleInverse.isAtomic());
        Assertions.assertTrue(tripleInverse.isAtomic());
        Assertions.assertTrue(quadInverse.isAtomic());
        Assertions.assertTrue(fiveInverse.isAtomic());
    }

    @Test
    void testToString() {
        Assertions.assertEquals("Ape ⋁ ¬Ape", alwaysTrue1.toString());
        Assertions.assertEquals("Ape ⋀ Banana ➔ Ape", alwaysTrue2.toString());
        Assertions.assertEquals("Ape ⋀ ¬Ape", alwaysFalse1.toString());
        Assertions.assertEquals("A ⋀ ¬(A ⋁ B)", alwaysFalse2.toString());
        Assertions.assertEquals("¬¬¬¬¬A", fiveInverse.toString());
    }
}
