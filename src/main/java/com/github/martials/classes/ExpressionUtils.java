package com.github.martials.classes;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ExpressionUtils {

    @NotNull
    public static Expression simplify(@NotNull String stringExp, boolean simplify) {
        Expression.setOrderOfOperations(new ArrayList<>()); // Resets the orderOfOperations

        final Expression exp = simplifyRec(stringExp, simplify);
        if (!exp.getLeading().contains("¬")) {
            exp.setLeading("");
            exp.setTrailing("");
        }
        return exp;
    }

    @NotNull
    private static Expression simplifyRec(@NotNull String stringExp, boolean simplify) {

        Expression exp = new Expression();

        // Basis
        if (isAtomic(stringExp)) {
            while (stringExp.contains("¬")) {
                stringExp = stringExp.replace("¬", "");
                exp.appendLeading("¬");
            }
            if (stringExp.contains("(") || stringExp.contains(")")) {
                stringExp = stringExp.replaceAll("^[()]$", ""); // TODO test
            }
            exp.setAtomic(stringExp);
            if (simplify) {
                exp.doubleNegation();
            }
            return exp;
        }

        // TODO move this above the basis?
        while (stringExp.charAt(0) == '¬' && isOuterParentheses(stringExp.substring(1))) {
            stringExp = stringExp.replace("¬", "");
            exp.appendLeading("¬");
        }

        int oldStringLen = stringExp.length();
        stringExp = removeOuterParentheses(stringExp);

        if (oldStringLen != stringExp.length()) {
            exp.appendLeading("(");
            exp.appendTrailing(")");
        }

        CenterOperator center = getCenterOperatorIndex(stringExp);

        exp.setLeft(simplifyRec(stringExp.substring(0, center.index()), simplify)); // Left
        exp.setOperator(center.operator());
        exp.setRight(simplifyRec(stringExp.substring(center.index() + 1), simplify)); // Right

        if (simplify) {
            exp.laws();
        }
        // Moves expressions up the tree structure
        if (exp.getRight() == null) {
            exp = exp.getLeft();
        }
        else if (exp.getLeft().getLeft() != null && exp.getLeft().getRight() == null) {
            exp.setLeft(exp.getLeft().getLeft());
        }
        else if (exp.getRight().getLeft() != null && exp.getRight().getRight() == null) {
            exp.setRight(exp.getRight().getLeft());
        }

        exp.commutativeProperty(); // Sorts the expression
        exp.removeParenthesis();
        return exp;
    }

    private static boolean isAtomic(@NotNull String exp) {

        final Pattern regex = Pattern.compile("^[a-zA-ZæøåÆØÅ0-9\\[\\]]$");
        boolean atomic = regex.matcher(exp).matches();
        int nrOfAtomics = 0;
        boolean isSquareBracket = false;

        for (int i = 0; atomic && i < exp.length(); i++) {
            if (exp.charAt(i) == '[') {
                isSquareBracket = true;
            }
            else if (exp.charAt(i) == ']') {
                nrOfAtomics++;
                isSquareBracket = false;
                if (nrOfAtomics > 1) {
                    atomic = false;
                }
            }
            else if (regex.matcher(Character.toString(exp.charAt(i))).matches() && !isSquareBracket) {
                nrOfAtomics++;
                if (nrOfAtomics > 1) {
                    atomic = false;
                }
            }
        }
        return atomic;
    }

    /**
     * Iterates through the string and finds the outer most center operator, if there are two, the one with the lowest weight is picked.
     * If they have the same weight the one to the right is picked.
     *
     * @param stringExp A truth expression as a string, with no spaces between characters
     * @return The index position of the center operator based on the weight of the operators
     */
    @NotNull
    private static CenterOperator getCenterOperatorIndex(@NotNull String stringExp) {

        stringExp = removeOuterParentheses(stringExp);

        final List<CenterOperator> operators = new ArrayList<>();
        for (int i = 0; i < stringExp.length(); i++) {

            int parentheses = 0;

            // Skips all lines within parenthesis
            char c = stringExp.charAt(i);
            while (c == '(' || c == '[' || parentheses > 0) {
                c = stringExp.charAt(i);
                if (c == '(' || c == '[') {
                    parentheses++;
                }
                else if (c == ')' || c == ']') {
                    parentheses--;
                }
                i++;
            }

            // Finds the matching Operator
            Operator operator = Operator.getOperator(stringExp.charAt(i));
            if (operator != null && operator != Operator.not) {
                operators.add(new CenterOperator(operator, i));
            }
        }

        CenterOperator op = operators.get(0);
        boolean allEqual = true;

        // Finds the rightmost operator with the lowest weight, if all the operators are equal, pick the center most
        for (int i = 1; i < operators.size(); i++) {
            if (operators.get(i).operator().getWeight() != op.operator().getWeight()) {
                allEqual = false;
            }
            if (operators.get(i).operator().getWeight() <= op.operator().getWeight()) {
                op = operators.get(i);
            }
        }
        return allEqual ? operators.get(operators.size() / 2) : op;
    }

    /**
     * TODO Gonna need some cleaning
     * Checks if a string is a valid truth expression. If the string is valid, it will return "empty string", otherwise an error message
     * It is not true if either of the following are present.
     * More than one operator in a row.
     * More than one atomic value in a row.
     * Not operator prior to another different operator.
     * The parentheses do not match.
     *
     * @param stringExp A string in the style of a truth expression
     *                  //     * @param illegalChar      A string message for illegal characters
     *                  //     * @param missingChar      A string message for missing characters
     *                  //     * @param atIndex          A string message for displaying index
     *                  //     * @param expressionTooBig A string message when the expression is too big
     */
    @NotNull
    public static String isLegalExpression(@NotNull String stringExp) {

        // TODO translate using language in header
        final String illegalChar = "Illegal character",
                missingChar = "Missing character",
                atIndex = "at index:",
                expressionTooBig = "Expression too big";

        final Pattern regex = Pattern.compile("^[^a-zA-ZæøåÆØÅ0-9()⋁⋀➔¬\\[\\]]|]\\[|\\)\\[|\\)\\(|\\(\\)$");
        final Matcher matcher = regex.matcher(stringExp);

        boolean isMatch = matcher.matches();
        if (isMatch) {
            String match = matcher.group();
            return error(match.charAt(0), stringExp.indexOf(match), 0, illegalChar, atIndex);
        }

        Stack<Character> brackets = new Stack<>();
        boolean isTruthValue = false, insideSquare = false;
        int numberOfOperators = 0;

        for (int i = 0; i < stringExp.length(); i++) {
            char charAtI = stringExp.charAt(i);

            if (!insideSquare && Operator.isOperator(charAtI) && charAtI != '¬') {
                numberOfOperators++;
                if (numberOfOperators > 9) {
                    return expressionTooBig;
                }
            }

            if (charAtI == '(' || charAtI == '[') {
                if (i > 0 && !Operator.isOperator(stringExp.charAt(i - 1)) && !isParentheses(stringExp.charAt(i - 1))) {
                    return error(charAtI, i, 20, illegalChar, atIndex);
                }
                if (charAtI == '[') {
                    char top = brackets.pop();
                    if (brackets.peek() == '[') {
                        return error(charAtI, i, 21, illegalChar, atIndex);
                    }
                    brackets.push(top);
                    insideSquare = true;
                }
                brackets.push(charAtI);
            }
            else if (charAtI == ')' || charAtI == ']') {
                char pop = brackets.pop();
                if (charAtI == ']') {
                    insideSquare = false;
                }
                if (charAtI == ')' && pop != '(' || charAtI == ']' && pop != '[') {
                    return error(charAtI, i, 22, illegalChar, atIndex);
                }
            }
            else if (!Operator.isOperator(charAtI) && !isParentheses(charAtI)) {
                isTruthValue = true;
            }

            if (i > 0 && !insideSquare) {
                char prevChar = stringExp.charAt(i - 1);

                if (Operator.not.getOperator() == charAtI) {
                    if (!Operator.isOperator(prevChar) && prevChar != '(' || i == stringExp.length() - 1) {
                        return error(charAtI, i, 30, illegalChar, atIndex);
                    }
                    continue;
                }

                // Return if two operators are following eachother, but not ¬
                if (Operator.isOperator(charAtI) &&
                        (Operator.isOperator(prevChar) || prevChar == '(' || i == stringExp.length() - 1)) {
                    return error(charAtI, i, 40, illegalChar, atIndex);
                }
                // Return if two atomic values are following eachother
                else if (!(charAtI == ']' || Operator.isOperator(charAtI) || Operator.isOperator(prevChar) ||
                        isParentheses(charAtI) || isParentheses(prevChar))) {
                    return error(charAtI, i, 50, illegalChar, atIndex);
                }
            }
        }
        if (!isTruthValue) {
            return error('A', stringExp.length(), 23, missingChar, atIndex);
        }
        if (brackets.size() > 0) {
            return error(brackets.pop() == '(' ? ')' : ']', stringExp.length(), 24, missingChar, atIndex);
        }

        return ""; // Legal expression
    }

    @NotNull
    private static String error(char c, int index, int errorCode, @NotNull String errorMsg, @NotNull String atIndex) {
        String error = errorMsg + " '" + c + "' " + atIndex + " " + index;
        printErr(error, errorCode);
        return error;
    }

    private static void printErr(@NotNull String error, int errorCode) {
        System.err.println(error + ". code=" + errorCode);
    }

    private static boolean isParentheses(char c) {
        return c == '(' || c == ')';
    }

    private static String removeOuterParentheses(@NotNull String stringExp) {
        return isOuterParentheses(stringExp) ? stringExp.substring(1, stringExp.length() - 1) : stringExp;
    }

    private static boolean isOuterParentheses(@NotNull String stringExp) {
        int operators = 0;
        boolean is = false;
        int index = 0;

        while (stringExp.charAt(index) == '¬') {
            index++;
        }

        if (stringExp.charAt(index) == '(') {
            is = true;
        }
        while (is && (stringExp.charAt(index) == '(' || operators > 0)) {
            if (stringExp.charAt(index) == '(') {
                operators++;
            }
            else if (stringExp.charAt(index) == ')') {
                operators--;
                if (operators == 0 && index != stringExp.length() - 1) {
                    is = false;
                }
            }
            index++;
        }
        return is;
    }

    @NotNull
    public static String replaceOperators(@NotNull String exp) {

        int startIndex = 0;

        for (int i = 1; i < exp.length(); i++) {
            if (exp.charAt(i) == '[') {
                exp = regex(exp, startIndex, i);
            }
            else if (exp.charAt(i) == ']') {
                startIndex = i + 1;
            }
        }
        exp = regex(exp, startIndex);

        return exp;
    }

    @NotNull
    private static String regex(@NotNull String exp, int start, int end) {
        if (start < end) {
            for (var operator : Operator.getPredefined()) {
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
