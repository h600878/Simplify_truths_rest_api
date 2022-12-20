package com.github.martials.expressions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.martials.enums.Hide;
import com.github.martials.enums.Sort;
import com.github.martials.utils.ExpressionUtils;
import com.github.martials.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

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

    public TruthTable(@NotNull Expression[] expressions, Sort sort) {
        this(expressions, Hide.NONE, sort);
    }

    public TruthTable(@NotNull Expression[] expressions, Hide hide) {
        this(expressions, hide, Sort.DEFAULT);
    }

    @NotNull
    public static boolean[][] helperMatrix(int numberOfAtomics) {
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
    public boolean[][] create(Hide hide, Sort sort) { // TODO implement hide and sort

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
                if (exp.isAtomic() && StringUtils.numberOfChar(exp.getLeading(), '¬') % 2 == 0) {
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
                    boolean exists = false;
                    if (index != -1) {
                        exists = truthMatrix[row][index];
                    }
                    truthMatrix[row][column] = !exists;
                }
                else {
                    final boolean left = truthMatrix[row][findExp(expressions, exp.getLeft())];
                    final boolean right = truthMatrix[row][findExp(expressions, exp.getRight())];

                    final boolean boolExp = exp.solve(left, right);

                    if (exp == expressions[expressions.length - 1] && (hide == Hide.TRUE && boolExp ||
                            hide == Hide.FALSE && !boolExp)) {
                        truthMatrix[row] = null;
                    }
                    else {
                        truthMatrix[row][column] = boolExp;

                        if (exp == expressions[expressions.length - 1] &&
                                (sort == Sort.TRUE_FIRST && boolExp || sort == Sort.FALSE_FIRST && !boolExp)) {
                            int r = row;
                            while (r > 0 && (truthMatrix[r - 1] == null || truthMatrix[r - 1][expressions.length - 1] == !boolExp)) {
                                final boolean[] help = truthMatrix[r];
                                truthMatrix[r] = truthMatrix[r - 1];
                                truthMatrix[r - 1] = help;
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

    // Finds the location of an expression, then checks the value
    private int findExp(@NotNull Expression[] expressions, @NotNull Expression exp) {
        for (int i = 0; i < expressions.length; i++) {

            if (exp.equals(expressions[i])) {
                return i;
            }
        }
        return -1;
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
