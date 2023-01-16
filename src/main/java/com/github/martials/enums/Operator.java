package com.github.martials.enums;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.regex.Pattern;

public enum Operator {
    IMPLICATION('➔', 0, new String[] {"->", "implication", "imp", "impliserer", "=>"}, Pattern.compile("->|=>|impliserer|implication|imp")),
    OR('⋁', 1, new String[] {"/", "or", "eller", "intersection", "snitt"}, Pattern.compile("/|or|eller|intersection|snitt")),
    AND('⋀', 2, new String[] {"&", "and", "og", "union"}, Pattern.compile("&|and|og|union")),
    NOT('¬', 3, new String[] {"not", "ikke", "!", "~"}, Pattern.compile("[!~]|not|ikke"));

    private final char operator;
    private final int weight;
    private final String[] values; // TODO remove and use regex
    private final Pattern regex;

    Operator(char operator, int weight, String[] values, Pattern regex) {
        this.operator = operator;
        this.weight = weight;
        this.values = values;
        this.regex = regex;
    }

    @Nullable
    public static Operator getOperator(char operator) {
        return getOperator(Character.toString(operator));
    }

    @Nullable
    public static Operator getOperator(@NotNull String operator) {
        for (Operator value : values()) {
            final boolean inArray = Arrays.asList(value.getValues()).contains(operator);
            if (operator.equals(Character.toString(value.operator)) || inArray) {
                return value;
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
        return Arrays.stream(values())
                .anyMatch(operator -> op.equals(Character.toString(operator.operator)) ||
                        Arrays.asList(operator.values).contains(op));
    }

    public char getOperator() {
        return operator;
    }

    public int getWeight() {
        return weight;
    }

    public String[] getValues() {
        return values;
    }

    public Pattern getRegex() {
        return regex;
    }

    @Override
    public String toString() {
        return Character.toString(operator);
    }
}
