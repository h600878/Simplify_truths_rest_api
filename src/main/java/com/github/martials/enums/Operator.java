package com.github.martials.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;

/**
 * An enum representing the operators used in the expression.
 * The ordinal of the enum determines the weight of the operator. 0 is the highest weight, and will be evaluated first.
 *
 * @author Martin Berg Alstad
 */
@Schema(name = "Operator", description = "An enum representing the operators used in the expression. The current operators are: AND(&), OR(/), IMPLICATION(->), NOT(!)")
public enum Operator {
    IMPLICATION('➔', Pattern.compile("->"), (a, b) -> !a || b),
    OR('⋁', Pattern.compile("/"), (a, b) -> a || b),
    AND('⋀', Pattern.compile("&"), (a, b) -> a && b),
    NOT('¬', Pattern.compile("!"), (a, b) -> !a);

    private final char operator;
    private final Pattern regex;
    private final BiPredicate<Boolean, Boolean> predicate;

    Operator(char operator, Pattern regex, BiPredicate<Boolean, Boolean> predicate) {
        this.operator = operator;
        this.regex = regex;
        this.predicate = predicate;
    }

    public boolean test(boolean a, boolean b) {
        return predicate.test(a, b);
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

    public Pattern getRegex() {
        return regex;
    }

    @Override
    public String toString() {
        return Character.toString(operator);
    }
}
