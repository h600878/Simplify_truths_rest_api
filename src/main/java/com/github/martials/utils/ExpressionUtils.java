package com.github.martials.utils;

import com.github.martials.enums.Language;
import com.github.martials.expressions.CenterOperator;
import com.github.martials.expressions.Expression;
import com.github.martials.expressions.Operator;
import com.github.martials.expressions.OrderOperations;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionUtils {

    @NotNull
    private final List<OrderOperations> operations;
    private String expression;
    private boolean simplify;
    private final Language language;

    public ExpressionUtils() {
        this(null);
    }

    public ExpressionUtils(String expression) {
        this(expression, true);
    }

    public ExpressionUtils(@Nullable String expression, boolean simplify) {
        this(expression, simplify, Language.norwegianBokmaal);
    }

    public ExpressionUtils(@Nullable String expression, boolean simplify, Language language) {
        operations = new ArrayList<>();
        this.expression = expression;
        this.simplify = simplify;
        this.language = language;
    }

    public Expression simplify(String expression) {
        setExpression(expression);
        return simplify();
    }

    @NotNull
    public Expression simplify() {
        assert expression != null : "Expression cannot be null";

        final Expression exp = simplifyRec(expression, simplify);
        if (!exp.getLeading().contains("¬")) {
            exp.setLeading("");
            exp.setTrailing("");
        }
        return exp;
    }

    @NotNull
    private Expression simplifyRec(@NotNull String stringExp, boolean simplify) {

        Expression exp = new Expression();

        // Basis
        if (isAtomic(stringExp)) {
            while (stringExp.contains("¬")) {
                stringExp = stringExp.replaceFirst("¬", "");
                exp.appendLeading("¬");
            }
            if (stringExp.contains("(") || stringExp.contains(")")) {
                stringExp = stringExp.replaceAll("^[()]$", "");
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

        final int oldStringLen = stringExp.length();
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
            exp.laws(operations, language);
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

        exp.commutativeProperty(operations); // Sorts the expression
        exp.removeParenthesis(operations);
        return exp;
    }

    public static int getNumberOfUniqueAtomics(@NotNull Expression[] expressions) {
        int numberOfAtomics = 0;

        for (int i = 0; i < expressions.length; i++) {
            if (expressions[i].isAtomic()) {
                boolean exists = false;
                for (int j = i - 1; !exists && j >= 0; j--) {

                    // If the opposite expression already exists
                    if (expressions[j].isAtomic() && expressions[i].equalsAndOpposite(expressions[j])) {
                        exists = true;
                    }
                }
                if (!exists) {
                    numberOfAtomics++;
                }
            }
        }
        return numberOfAtomics;
    }

    private boolean isAtomic(@NotNull String exp) {

        if (!exp.matches("^.*[⋁⋀➔].*$")) {
            return true;
        }

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
    private CenterOperator getCenterOperatorIndex(@NotNull String stringExp) {

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

            if (i < stringExp.length()) {
                // Finds the matching Operator
                final Operator operator = Operator.getOperator(stringExp.charAt(i));
                if (operator != null && !Objects.equals(operator, Operator.not)) {
                    operators.add(new CenterOperator(operator, i));
                }
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
     */
    @NotNull
    public String isLegalExpression() { // TODO can be simplified with regex
        assert expression != null : "Expression cannot be null";

        boolean isEnglish = language == Language.english;

        final String illegalChar = isEnglish ? "Illegal character" : "Ugyldig tegn",
                missingChar = isEnglish ? "Missing character" : "Manglende tegn",
                atIndex = isEnglish ? "at index:" : "ved indeks:",
                expressionTooBig = isEnglish ? "Expression too big" : "Uttrykket er for stort";

        final Pattern regex = Pattern.compile("^[^a-zA-ZæøåÆØÅ0-9()⋁⋀➔¬\\[\\]]|]\\[|\\)\\[|\\)\\(|\\(\\)$");
        final Matcher matcher = regex.matcher(expression);

        boolean isMatch = matcher.find();
        if (isMatch) {
            String match = matcher.group();
            return error(match.charAt(0), expression.indexOf(match), 0, illegalChar, atIndex);
        }

        Stack<Character> brackets = new Stack<>();
        boolean isTruthValue = false, insideSquare = false;
        int numberOfOperators = 0;

        for (int i = 0; i < expression.length(); i++) {
            char charAtI = expression.charAt(i);

            if (!insideSquare && Operator.isOperator(charAtI) && charAtI != '¬') {
                if (i == 0) {
                    return error(charAtI, i, 10, illegalChar, atIndex);
                }
                numberOfOperators++;
                if (numberOfOperators > 9) {
                    return expressionTooBig;
                }
            }

            if (charAtI == '(' || charAtI == '[') {
                if (i > 0 && !Operator.isOperator(expression.charAt(i - 1)) && !isParentheses(expression.charAt(i - 1))) {
                    return error(charAtI, i, 20, illegalChar, atIndex);
                }
                if (charAtI == '[') {
                    try {
                        char top = brackets.pop();
                        if (brackets.peek() == '[') {
                            return error(charAtI, i, 21, illegalChar, atIndex);
                        }
                        brackets.push(top);
                    }
                    catch (EmptyStackException ignored) {
                    }
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
                char prevChar = expression.charAt(i - 1);

                if (Operator.not.getOperator() == charAtI) {
                    if (!Operator.isOperator(prevChar) && prevChar != '(' || i == expression.length() - 1) {
                        return error(charAtI, i, 30, illegalChar, atIndex);
                    }
                    continue;
                }

                // Return if two operators are following eachother, but not ¬
                if (Operator.isOperator(charAtI) &&
                        (Operator.isOperator(prevChar) || prevChar == '(' || i == expression.length() - 1)) {
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
            return error('A', expression.length(), 23, missingChar, atIndex);
        }
        if (brackets.size() > 0) {
            return error(brackets.pop() == '(' ? ')' : ']', expression.length(), 24, missingChar, atIndex);
        }

        return ""; // Legal expression
    }

    @NotNull
    private String error(char c, int index, int errorCode, @NotNull String errorMsg, @NotNull String atIndex) {
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
        while (is && index < stringExp.length() && (stringExp.charAt(index) == '(' || operators > 0)) {
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

    public boolean isSimplify() {
        return simplify;
    }

    public void setSimplify(boolean simplify) {
        this.simplify = simplify;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    @NotNull
    public List<OrderOperations> getOperations() {
        return operations;
    }
}
