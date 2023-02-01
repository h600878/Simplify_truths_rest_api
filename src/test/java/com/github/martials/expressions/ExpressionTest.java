package com.github.martials.expressions;

import com.github.martials.utils.ExpressionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ExpressionTest {

    private Expression
            alwaysTrue1, alwaysTrue2,
            alwaysFalse1, alwaysFalse2,
            doubleInverse, tripleInverse, quadInverse, fiveInverse,
            aAndA, aOrA, aAndAOrA, aImpliesNotA,
            notAandNotB, notAorNotB,
            aAndBorBandC, aOrBandBorC,
            parenthesesAandB,
            wrongOrder,
            notAAndNotCOrA;

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
        notAAndNotCOrA = new ExpressionUtils("¬A⋀¬C⋁A", false).simplify();
    }

    @Test
    void testEquals() {

        assertNotEquals(alwaysTrue1, alwaysTrue2);
        assertNotEquals(alwaysFalse1, alwaysFalse2);

        assertEquals(alwaysFalse1, alwaysFalse1);
    }

    @Test
    void equalsAndOpposite() {

        assertFalse(alwaysTrue1.equalsAndOpposite(alwaysTrue2));
        assertFalse(alwaysFalse1.equalsAndOpposite(alwaysFalse1));

        assertTrue(alwaysTrue1.equalsAndOpposite(alwaysFalse1));
    }

    @Test
    void removeParenthesis() {
        parenthesesAandB.removeParenthesis(new ArrayList<>());
        assertEquals("A ⋀ B", parenthesesAandB.toString());

        notAandNotB.deMorgansLaws();
        notAandNotB.removeParenthesis(new ArrayList<>());
        assertEquals("¬(A ⋁ B)", notAandNotB.toString());
        notAorNotB.deMorgansLaws();
        notAorNotB.removeParenthesis(new ArrayList<>());
        assertEquals("¬(A ⋀ B)", notAorNotB.toString());
    }

    @Test
    void distributiveProperty() {
        aOrBandBorC.distributiveProperty();
        assertEquals("B ⋁ A ⋀ C", aOrBandBorC.toString());
        aAndBorBandC.distributiveProperty();
        assertEquals("B ⋀ (A ⋁ C)", aAndBorBandC.toString());
    }

    @Test
    void deMorgansLaws() {
        notAandNotB.deMorgansLaws();
        assertEquals("¬(A ⋁ B)", notAandNotB.toString());
        notAorNotB.deMorgansLaws();
        assertEquals("¬(A ⋀ B)", notAorNotB.toString());
        notAAndNotCOrA.getLeft().deMorgansLaws();
        assertEquals("¬(A ⋁ C) ⋁ A", notAAndNotCOrA.toString());
    }

    @Test
    void isInverse() {
        assertTrue(tripleInverse.isInverse());
        assertTrue(fiveInverse.isInverse());
    }

    @Test
    void isNotInverse() {
        assertFalse(doubleInverse.isInverse());
        assertFalse(quadInverse.isInverse());
    }

    @Test
    void commutativeProperty() {
        wrongOrder.commutativeProperty(new ArrayList<>());
        assertEquals("A ⋁ B", wrongOrder.toString());
    }

    @Test
    void eliminationOfImplication() {
        aImpliesNotA.eliminationOfImplication();
        assertEquals("¬A ⋁ ¬A", aImpliesNotA.toString());
    }

    @Test
    void absorptionLaw() {
        aAndA.absorptionLaw();
        assertEquals("A", aAndA.toString());
        aOrA.absorptionLaw();
        assertEquals("A", aOrA.toString());
        aImpliesNotA.eliminationOfImplication();
        aImpliesNotA.absorptionLaw();
        assertEquals("¬A", aImpliesNotA.toString());
        aAndAOrA.absorptionLaw();
        assertEquals("A", aAndAOrA.toString());
        notAAndNotCOrA.absorptionLaw();
        assertEquals("¬C ⋁ A", notAAndNotCOrA.toString());
    }

    @Test
    void absoptionLawAfterDeMorgan() {
        notAAndNotCOrA.getLeft().deMorgansLaws();
        notAAndNotCOrA.absorptionLaw();
        assertEquals("¬C ⋁ A", notAAndNotCOrA.toString());
    }

    @Test
    void evenNegation() {
        doubleInverse.doubleNegation();
        quadInverse.doubleNegation();
        assertEquals("", doubleInverse.getLeading());
        assertEquals("", quadInverse.getLeading());
    }

    @Test
    void oddNegation() {
        tripleInverse.doubleNegation();
        fiveInverse.doubleNegation();
        assertEquals("¬", tripleInverse.getLeading());
        assertEquals("¬", fiveInverse.getLeading());
    }

    @Test
    void getNumberOfAtomics() {
        assertEquals(1, fiveInverse.getNumberOfAtomics());
        assertEquals(2, alwaysTrue1.getNumberOfAtomics());
        assertEquals(3, alwaysTrue2.getNumberOfAtomics());
    }

    @Test
    void solveAlwaysTrue() {
        final boolean[] booleans = new boolean[] {false, true};

        for (boolean l : booleans) {
            for (boolean r : booleans) {
                assertTrue(alwaysTrue1.solve(l, !l), "l: " + l + ", r: " + r);
                assertTrue(alwaysTrue2.solve(l, l || r), "l: " + l + ", r: " + r);
            }
        }
    }

    @Test
    void solveOneIsTrueIfOr() {
        assertTrue(wrongOrder.solve(false, true));
        assertTrue(wrongOrder.solve(true, false));
    }

    @Test
    void solveNeitherTrue() {
        assertFalse(wrongOrder.solve(false, false));
    }

    @Test
    void isAtomic() {
        assertFalse(alwaysTrue1.isAtomic());
        assertFalse(alwaysTrue2.isAtomic());
        assertFalse(alwaysFalse1.isAtomic());
        assertFalse(alwaysFalse2.isAtomic());
    }

    @Test
    void isNotAtomic() {
        assertTrue(doubleInverse.isAtomic());
        assertTrue(tripleInverse.isAtomic());
        assertTrue(quadInverse.isAtomic());
        assertTrue(fiveInverse.isAtomic());
    }

    @Test
    void testToString() {
        assertEquals("Ape ⋁ ¬Ape", alwaysTrue1.toString());
        assertEquals("Ape ⋀ Banana ➔ Ape", alwaysTrue2.toString());
        assertEquals("Ape ⋀ ¬Ape", alwaysFalse1.toString());
        assertEquals("A ⋀ ¬(A ⋁ B)", alwaysFalse2.toString());
        assertEquals("¬¬¬¬¬A", fiveInverse.toString());
    }
}
