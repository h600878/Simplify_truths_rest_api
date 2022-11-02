package com.github.martials.expressions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Remember strength: ¬, ⋀, ⋁, ➔
 * TODO add more operators: XOR, NOR...
 */
public class Operator { // TODO remove values[] and only use regex?

    private final char operator;
    private final int weight;
    private String[] values;
    private Pattern regex;

    public static final Operator implication;
    public static final Operator or;
    public static final Operator and;
    public static final Operator not;

    public Operator(char operator, int weight, String[] values, @NotNull String regex) {
        this.operator = operator;
        this.weight = weight;
        this.values = values;
        this.regex = Pattern.compile(regex);
    }

    public Operator(char operator, int weight) {
        this(operator, weight, null, "");
    }

    static {
        implication = new Operator('➔', 0, new String[] {"->", "implication", "imp", "impliserer", "=>"},
                Pattern.compile("^->|=>|imp|implication|impliserer$").pattern());

        or = new Operator('⋁', 1, new String[] {"|", "or", "eller", "intersection", "snitt", "\\/"},
                Pattern.compile("^\\||or|eller|intersection|snitt|\\/$").pattern());

        and = new Operator('⋀', 2, new String[] {"&", "and", "og", "union", "/\\"},
                Pattern.compile("^&|and|og|union|/\\$").pattern());

        not = new Operator('¬', 3, new String[] {"not", "ikke", "!", "~"},
                Pattern.compile("^[!~]|not|ikke$").pattern());
    }

    @NotNull
    public static Operator[] getPredefined() {
        return new Operator[] {implication, or, and, not};
    }

    @Nullable
    public static Operator getOperator(char operator) {
        for (var value : getPredefined()) {
            if (operator == value.operator) {
                return value;
            }
        }
        return null;
    }

    @Nullable
    public static Operator getOperator(@NotNull String operator) {
        for (var value : getPredefined()) {
            final boolean inArray = Arrays.asList(value.getValues()).contains(operator);
            if (operator.equals(Character.toString(value.operator)) || inArray) {
                return value;
            }
        }
        return null;
    }

    /**
     * Checks if a single char is used as an operator
     * @param op A single character
     * @return True if the char is used to represent an operator
     */
    public static boolean isOperator(char op) {
        return Arrays.stream(getPredefined())
                .anyMatch(operator -> op == operator.operator ||
                        Arrays.asList(operator.values).contains(Character.toString(op)));
    }

    /**
     * Checks if a string is used as an operator. Case sensitive, lowercase expected.
     * @param op A string
     * @return True if the string is used to represent an operator
     */
    public static boolean isOperator(@NotNull String op) {
        return Arrays.stream(getPredefined())
                .anyMatch(operator -> op.equals(Character.toString(operator.operator)) ||
                        Arrays.asList(operator.values).contains(op));
    }

    @NotNull
    @Override
    public String toString() {
        return Character.toString(operator);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Operator operator1 = (Operator) o;

        if (operator != operator1.operator) return false;
        if (weight != operator1.weight) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(values, operator1.values)) return false;
        return Objects.equals(regex, operator1.regex);
    }

    @Override
    public int hashCode() {
        int result = operator;
        result = 31 * result + weight;
        result = 31 * result + Arrays.hashCode(values);
        result = 31 * result + (regex != null ? regex.hashCode() : 0);
        return result;
    }

    public char getOperator() {
        return operator;
    }

    public int getWeight() {
        return weight;
    }

    @NotNull
    public String[] getValues() {
        return values;
    }

    public void setValues(@NotNull String[] values) {
        this.values = values;
    }

    @NotNull
    @JsonIgnore
    public Pattern getRegex() {
        return regex;
    }

    public void setRegex(@NotNull Pattern regex) {
        this.regex = regex;
    }
}
