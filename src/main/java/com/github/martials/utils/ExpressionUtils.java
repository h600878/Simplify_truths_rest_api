package com.github.martials.utils;

import com.github.martials.controllers.ApiController;
import com.github.martials.enums.Language;
import com.github.martials.enums.Operator;
import com.github.martials.exceptions.IllegalCharacterException;
import com.github.martials.exceptions.MissingCharacterException;
import com.github.martials.exceptions.TooBigExpressionException;
import com.github.martials.expressions.CenterOperator;
import com.github.martials.expressions.Expression;
import com.github.martials.expressions.OrderOperations;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionUtils {

    @NotNull
    private final List<OrderOperations> operations;
    private String expression;
    private boolean simplify;
    private final boolean caseSensitive;
    private final Language language;

    private static final int MAX_EXPRESSION_SIZE = 15;

    private static final Logger log = LoggerFactory.getLogger(ApiController.class);

    public ExpressionUtils() {
        this(null);
    }

    public ExpressionUtils(String expression) {
        this(expression, true);
    }

    public ExpressionUtils(@Nullable String expression, boolean simplify) {
        this(expression, simplify, Language.NORWEGIAN_BOKMAAL);
    }

    public ExpressionUtils(@Nullable String expression, boolean simplify, Language language) {
        this(expression, simplify, language, false);
    }

    public ExpressionUtils(@Nullable String expression, boolean simplify, Language language, boolean caseSensitive) {
        operations = new ArrayList<>();
        this.expression = expression;
        this.simplify = simplify;
        this.language = language;
        this.caseSensitive = caseSensitive;
    }

    @NotNull
    public Expression simplify(String expression) {
        setExpression(expression);
        return simplify();
    }

    @NotNull
    public Expression simplify() {
        assert expression != null : "Expression cannot be null";

        log.debug("Simplifying expression: {}", expression);
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
        exp.setCaseSensitive(caseSensitive);

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

        stringExp = stringExp.replaceAll(" ", "");
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

    public static int getNumberOfUniqueAtomics(@NotNull Expression[] expressions) { // TODO use regex Pattern Match?
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
        return !exp.matches("^.*[⋁⋀➔].*$");
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
            while (c == '(' || parentheses > 0) {
                c = stringExp.charAt(i);
                if (c == '(') {
                    parentheses++;
                }
                else if (c == ')') {
                    parentheses--;
                }
                i++;
            }

            if (i < stringExp.length()) {
                // Finds the matching Operator
                final Operator operator = Operator.getOperator(stringExp.charAt(i));
                if (operator != null && !Objects.equals(operator, Operator.NOT)) {
                    operators.add(new CenterOperator(operator, i));
                }
            }
        }

        CenterOperator op = operators.get(0);
        boolean allEqual = true;

        // Finds the rightmost operator with the lowest weight, if all the operators are equal, pick the center most
        for (int i = 1; i < operators.size(); i++) {
            if (operators.get(i).operator().ordinal() != op.operator().ordinal()) {
                allEqual = false;
            }
            if (operators.get(i).operator().ordinal() <= op.operator().ordinal()) {
                op = operators.get(i);
            }
        }
        return allEqual ? operators.get(operators.size() / 2) : op;
    }

    // TODO return better error messages

    /**
     * Checks if a string is a valid truth expression. If the string is valid, it will return "empty string", otherwise an error message
     * It is not true if either of the following are present.
     * More than one operator in a row.
     * More than one atomic value in a row.
     * Not operator prior to another different operator.
     * The parentheses do not match.
     *
     * @throws IllegalCharacterException If the string contains an illegal character, or missplaced chacater
     * @throws MissingCharacterException If the string is missing a character, or missing a parenthesis
     * @throws TooBigExpressionException If the expression has more than 15 parts
     */
    public void isLegalExpression() throws IllegalCharacterException, MissingCharacterException, TooBigExpressionException { // TODO Gonna need some cleaning, use regex!
        assert expression != null : "Expression cannot be null";

        final String atomicValues = "a-zA-ZæøåÆØÅ",
                legalCharacters = atomicValues + "0-9\\(\\)⋁⋀➔¬ _=-",
                illegalRegex = "|\\) *\\(|\\( *\\)|[⋀⋁¬] *[⋀⋁➔]|[⋀⋁➔¬] *$|[" + atomicValues + "] +[" + atomicValues + "]";
        final Pattern regex = Pattern.compile("[^" + legalCharacters + "]" + illegalRegex);
        final Matcher matcher = regex.matcher(expression);

        removeWhiteSpace();

        if (matcher.find()) {
            throw new IllegalCharacterException(language, matcher.group().charAt(0));
        }
        else if (expression.length() == 0 || Pattern.compile("^[^" + atomicValues + "]$").matcher(expression).find()) {
            throw new MissingCharacterException(language, 'A');
        }

        final Stack<Character> brackets = new Stack<>();
        int numberOfOperators = 0;

        for (int i = 0; i < expression.length(); i++) {
            char charAtI = expression.charAt(i);

            if (Operator.isOperator(charAtI) && charAtI != Operator.NOT.getOperator() && ++numberOfOperators > MAX_EXPRESSION_SIZE - 1) {
                throw new TooBigExpressionException(language);
            }

            if (charAtI == '(') {
                if (i > 0 && !Operator.isOperator(expression.charAt(i - 1)) && !isParentheses(expression.charAt(i - 1))) {
                    throw new IllegalCharacterException(language, charAtI);
                }
                brackets.push(charAtI);
            }
            else if (charAtI == ')') {
                char pop;
                try {
                    pop = brackets.pop();
                }
                catch (EmptyStackException e) {
                    throw new MissingCharacterException(language, '(');
                }

                if (pop != '(') {
                    throw new IllegalCharacterException(language, charAtI);
                }
            }

            if (i > 0) {
                char prevChar = expression.charAt(i - 1);

                if (Operator.NOT.getOperator() == charAtI) {
                    if (!Operator.isOperator(prevChar) && prevChar != '(' || i == expression.length() - 1) {
                        throw new IllegalCharacterException(language, charAtI);
                    }
                }
            }
        }
        if (brackets.size() > 0) {
            throw new MissingCharacterException(language, ')');
        }
    }

    private void removeWhiteSpace() {
        expression = expression.replaceAll(" ", "");
        log.debug("Removed whitespace from expression, {}", expression);
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

        while (stringExp.charAt(index) == Operator.NOT.getOperator()) {
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
