package com.github.martials.enums;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

public enum Operator {
    IMPLICATION('➔', 0, Pattern.compile("->")),
    OR('⋁', 1, Pattern.compile("/")),
    AND('⋀', 2, Pattern.compile("&")),
    NOT('¬', 3, Pattern.compile("!"));

    private final char operator;
    private final int weight;
    private final Pattern regex;

    Operator(char operator, int weight, Pattern regex) {
        this.operator = operator;
        this.weight = weight;
        this.regex = regex;
    }

    @Nullable
    public static Operator getOperator(char operator) {
        return getOperator(Character.toString(operator));
    }

    @Nullable
    public static Operator getOperator(@NotNull String operator) {
        for (Operator op : values()) {
            if (operator.matches(op.regex.pattern()) || Objects.equals(operator, op.operator + "")) {
                return op;
            }
        }
        return null;
    }

    /**
     * Checks if a single char is used as an operator
     *
     * @param op A single character
     * @return True if the char is used to represent an operator
     */
    public static boolean isOperator(char op) {
        return isOperator(Character.toString(op));
    }

    /**
     * Checks if a string is used as an operator. Case sensitive, lowercase expected.
     *
     * @param op A string
     * @return True if the string is used to represent an operator
     */
    public static boolean isOperator(@NotNull String op) {
        return Arrays.stream(Operator.values()).anyMatch(operator -> op.matches(operator.regex.pattern()) || operator.operator == op.charAt(0));
    }

    public char getOperator() {
        return operator;
    }

    public int getWeight() {
        return weight;
    }

    public Pattern getRegex() {
        return regex;
    }

    @Override
    public String toString() {
        return Character.toString(operator);
    }
}
