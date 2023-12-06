package no.martials.api.utils;

import no.martials.api.enums.Operator;
import no.martials.api.expressions.Expression;
import no.martials.api.expressions.TruthTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public abstract class StringUtils {

    private static final Logger log = LoggerFactory.getLogger(StringUtils.class);

    /**
     * Gets the number of a given character in a string
     *
     * @param string The string to be checked
     * @param c      The 'char' that the method will look for
     */
    public static int numberOfChar(@NotNull String string, char c) {
        int numberOf = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == c) {
                numberOf++;
            }
        }
        return numberOf;
    }

    @NotNull
    public static String formatString(@NotNull String expression, boolean caseSensitive) {
        if (!caseSensitive) {
            expression = expression.toLowerCase();
            log.debug("Expression converted to lowercase: {}", expression);
        }

        expression = StringUtils.replaceOperators(expression);
        log.debug("Expression changed to: {}", expression);
        return expression;
    }

    @NotNull
    public static String replaceOperators(@NotNull String exp) {

        for (Operator op : Operator.values()) {
            exp = exp.replaceAll(op.getInputOperator(), String.valueOf(op.getOutputOperator()));
        }
        return exp;
    }

    @NotNull
    public static String capitalizeFirstLetter(@NotNull String string) {
        if (string.isEmpty()) {
            return "";
        }
        return string.substring(0, 1).toUpperCase() + (string.length() > 1 ? string.substring(1) : "");
    }

    @Nullable
    public static String[] mapToStrings(TruthTable table) {
        if (table == null) {
            return null;
        }
        return Arrays.stream(table.getExpressions())
                .map(Expression::toString)
                .toArray(String[]::new);
    }

}
