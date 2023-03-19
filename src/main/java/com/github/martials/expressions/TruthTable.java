package com.github.martials.expressions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.martials.enums.Hide;
import com.github.martials.enums.Sort;
import com.github.martials.utils.ExpressionUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

@Schema(name = "TruthTable", description = "A truth table for a given expression")
public class TruthTable {

    private final Expression[] expressions;
    private final boolean[][] truthMatrix;

    public TruthTable(@NotNull Expression[] expressions, Hide hide, Sort sort) {
        this.expressions = expressions;
        this.truthMatrix = create(hide, sort);
    }

    public TruthTable(@NotNull Expression[] expressions) {
        this(expressions, Hide.NONE, Sort.DEFAULT);
    }

    @NotNull
    public static boolean[][] helperMatrix(int numberOfAtomics) {
        if (numberOfAtomics < 1) {
            throw new IllegalArgumentException("Number of atomics must be greater than 0");
        }

        final int powerToLength = (int) Math.pow(2, numberOfAtomics);
        final boolean[][] helperMatrix = new boolean[numberOfAtomics][powerToLength];

        int changeIndex = powerToLength / 2;

        // Creates a helper matrix with the correct truth values, in order to get all the different combinations
        for (int column = 0; column < helperMatrix.length; column++) {
            boolean boolValue = true;
            int counter = 0;

            for (int row = 0; row < helperMatrix[column].length; row++) {
                if (counter == changeIndex) {
                    boolValue = !boolValue;
                    counter = 0;
                }
                helperMatrix[column][row] = boolValue;
                counter++;
            }
            changeIndex /= 2;
        }
        return helperMatrix;
    }

    @NotNull
    public boolean[][] create(Hide hide, Sort sort) {

        boolean[][] helperMatrix = helperMatrix(ExpressionUtils.getNumberOfUniqueAtomics(expressions));

        int truthMatrixRowIndex = 0;
        int truthMatrixColIndex = 0;

        // Creates a matrix with the body of the table, using the helper matrix helperMatrix to fill in the correct values.
        // The expressions that aren't atomic, uses the atomic values to see if they're truthy
        boolean[][] truthMatrix = new boolean[helperMatrix[0].length][expressions.length];

        for (int row = 0; helperMatrix[0].length > 0 && row < helperMatrix[0].length; row++) {

            for (int column = 0; column < expressions.length && row < truthMatrix.length; column++) {

                final Expression exp = expressions[column];

                assert truthMatrix[row] != null;

                // If not using 'not' operator
                if (exp.isAtomic() && !exp.isInverse()) {
                    truthMatrix[row][column] = helperMatrix[truthMatrixRowIndex][truthMatrixColIndex];

                    // Iterates through the helperMatrix
                    truthMatrixRowIndex = (truthMatrixRowIndex + 1) % helperMatrix.length;

                    if (truthMatrixRowIndex == 0) {
                        truthMatrixColIndex = (truthMatrixColIndex + 1) % helperMatrix[truthMatrixRowIndex].length;
                    }
                }
                // If using 'not' operator
                else if (exp.isAtomic()) {

                    final int index = findExp(expressions, new Expression(exp.getLeft(), exp.getOperator(), exp.getRight(), exp.getAtomic()));
                    boolean oppositeExpVal = false;
                    if (index != -1) {
                        oppositeExpVal = truthMatrix[row][index];
                    }
                    truthMatrix[row][column] = !oppositeExpVal;
                }
                else {
                    final boolean left = resolveResult(truthMatrix[row], exp.getLeft());
                    final boolean right = resolveResult(truthMatrix[row], exp.getRight());

                    final boolean expValue = exp.solve(left, right);

                    final int lastExpIndex = expressions.length - 1;
                    if (exp == expressions[lastExpIndex] && (hide == Hide.TRUE && expValue ||
                            hide == Hide.FALSE && !expValue)) {
                        truthMatrix[row] = null;
                    }
                    else {
                        truthMatrix[row][column] = expValue;

                        if (exp == expressions[lastExpIndex] &&
                                (sort == Sort.TRUE_FIRST && expValue || sort == Sort.FALSE_FIRST && !expValue)) {
                            int r = row; // TODO use better sort algorithm
                            while (r > 0 && (truthMatrix[r - 1] == null || truthMatrix[r - 1][lastExpIndex] == !expValue)) {
                                swap(truthMatrix, r, r - 1);
                                r--;
                            }
                        }
                    }
                }
            }
        }
        truthMatrix = Arrays.stream(truthMatrix).filter(Objects::nonNull).toArray(boolean[][]::new);
        return truthMatrix;
    }

    private void swap(@NotNull boolean[][] truthMatrix, int row1, int row2) {
        final boolean[] help = truthMatrix[row1];
        truthMatrix[row1] = truthMatrix[row2];
        truthMatrix[row2] = help;
    }

    /**
     * Finds the location of an expression, then checks the value
     *
     * @param expressions The expressions to search through
     * @param exp         The expression to find
     * @return The index of the expression, or -1 if not found
     */
    private int findExp(@NotNull Expression[] expressions, @NotNull Expression exp) {
        for (int i = 0; i < expressions.length; i++) {

            if (exp.equals(expressions[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Resolves the result of an expression, using the truth row
     * @param truthRow The truth row to search through
     * @param exp The expression to resolve
     * @return The result of the expression or false if not found
     */
    private boolean resolveResult(@NotNull boolean[] truthRow, @Nullable Expression exp) {
        if (exp == null) {
            return false;
        }

        boolean result = false;
        int index = findExp(expressions, exp);

        if (index == -1) {
            boolean left = resolveResult(truthRow, exp.getLeft());
            boolean right = resolveResult(truthRow, exp.getRight());
            result = exp.solve(left, right);
        }

        return index != -1 ? truthRow[index] : result;
    }

    @JsonIgnore
    public Expression[] getExpressions() {
        return expressions;
    }

    public boolean[][] getTruthMatrix() {
        return truthMatrix;
    }

    @Override
    public String toString() {
        StringBuilder table = new StringBuilder(Arrays.toString(expressions));

        table.append("\n\n");

        for (boolean[] row : truthMatrix) {
            table.append("|").append("\t");
            for (boolean col : row) {
                table.append(col).append("\t|\t");
            }
            table.append("\n");
        }

        return table.toString();
    }
}
