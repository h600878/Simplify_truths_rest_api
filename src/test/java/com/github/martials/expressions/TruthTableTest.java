package com.github.martials.expressions;

import com.github.martials.utils.ExpressionUtils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TruthTableTest {

    private TruthTable table;

    @BeforeEach
    void setUp() {
        table = new TruthTable(new ExpressionUtils("A ⋁ B ➔ ¬C", false).simplify().toSetArray());
    }

    @Test
    void helperMatrixEqualLengthRows() {
        boolean[][] matrix = TruthTable.helperMatrix(3);

        for (boolean[] row : matrix) {
            assertEquals(matrix[0].length, row.length, "Row length is not equal");
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void helperMatrixWithInvalidAtomics(int n) {
        assertThrows(IllegalArgumentException.class, () -> TruthTable.helperMatrix(n));
    }

    @Test
    void helperMatrixWithOneAtomic() {
        boolean[][] matrix = TruthTable.helperMatrix(1);

        assertEquals(1, matrix.length);
        assertEquals(2, matrix[0].length);
    }

    @Test
    void helperMatrixWithThreeAtomics() {
        boolean[][] matrix = TruthTable.helperMatrix(3);

        assertEquals(3, matrix.length);
        assertEquals(8, matrix[0].length);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
    void helperMatrixWithIncrementingAtomics(int n) {
        boolean[][] matrix = TruthTable.helperMatrix(n);

        assertEquals(n, matrix.length);
        assertEquals((int) Math.pow(2, n), matrix[0].length);
    }

    @Test
    void create() {
//        fail(); // TODO
    }

    @Test
    void testToString() {
        System.out.println(new TruthTable(new ExpressionUtils("A ⋁ B ➔ ¬C", false).simplify().toSetArray()));
    }
}