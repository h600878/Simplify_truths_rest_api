package com.github.martials.utils;

import com.github.martials.enums.Operator;
import org.jetbrains.annotations.NotNull;

public abstract class StringUtils {

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
    public static String replaceOperators(@NotNull String exp) {

        for (Operator op : Operator.values()) {
            exp = exp.replaceAll(op.getRegex().pattern(), op.getOperator() + "");
        }
        return exp;
    }

}
