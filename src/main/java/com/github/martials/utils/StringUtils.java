package com.github.martials.utils;

import com.github.martials.enums.Operator;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

        int startIndex = 0, endIndex = 0;

        List<String> dividers = List.of(" ", "&", "/", "->", "=>");

        while (startIndex != -1 && endIndex != -1) {
            startIndex = exp.indexOf(" ", ++endIndex); // TODO check all the dividers

            if (startIndex != -1) {
                startIndex++;
                endIndex = exp.indexOf(" ", startIndex);
                exp = regex(exp, startIndex, endIndex);
            }
        }

        return exp;
    }

    @NotNull
    private static String regex(@NotNull String exp, int start, int end) {
        if (start < end) {
            for (Operator operator : Operator.values()) {
                final String endOfString = exp.substring(end);
                final int startLength = exp.length();

                exp = exp.substring(0, start) +
                        exp.substring(start, end).replaceAll(operator.getRegex().pattern(),
                                Character.toString(operator.getOperator())) +
                        endOfString;

                // Subtracts the difference
                end -= Math.abs(startLength - exp.length());
            }
        }
        return exp;
    }

    @NotNull
    private static String regex(@NotNull String exp, int start) {
        return regex(exp, start, exp.length());
    }

}
