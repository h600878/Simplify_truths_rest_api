package com.github.martials.expressions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.martials.enums.Language;
import com.github.martials.enums.Operator;
import com.github.martials.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperties;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Schema(name = "Expression", description = "A tree structure that represents an expression, each node contains an operator and two expressions.")
@SchemaProperties(value = {
        @SchemaProperty(name = "left", schema = @Schema(implementation = Expression.class)),
        @SchemaProperty(name = "right", schema = @Schema(implementation = Expression.class)),
        @SchemaProperty(name = "operator", schema = @Schema(implementation = Operator.class)),
})
public class Expression { // TODO move some of the logic it's own class and extend it, left, right, operator, etc.

    @NotNull
    private String leading;
    @Nullable
    private Expression left;
    @Nullable
    private Operator operator;
    @Nullable
    private Expression right;
    @NotNull
    private String trailing;
    @Nullable
    private String atomic;
    private boolean caseSensitive;

    private final Logger log = LoggerFactory.getLogger(Expression.class);

    /**
     * @param leading  Leading content before the expression, like opening parentheses or 'not' operator
     * @param left     The expression to the left of the operator
     * @param operator The operator that separates the expression. Only 'and', 'or' and 'implication' can be used.
     * @param right    The expression to the right of the operator
     * @param trailing Trailing content after the expression, like closing parentheses
     * @param atomic   The atomic value of the expression, if the expression is not atomic this is 'null'.
     */
    public Expression(@NotNull String leading,
                      @Nullable Expression left,
                      @Nullable Operator operator,
                      @Nullable Expression right,
                      @NotNull String trailing,
                      @Nullable String atomic,
                      boolean caseSensitive) {
        assert operator != Operator.NOT : "Operator cannot be 'not'.";

        this.leading = leading;
        this.left = left;
        this.operator = operator;
        this.right = right;
        this.trailing = trailing;
        this.atomic = atomic;
        this.caseSensitive = caseSensitive;
        log.debug("Expression created={}", this);
    }

    public Expression(Expression left, Operator operator, Expression right, String atomic) {
        this("", left, operator, right, "", atomic, false);
    }

    public Expression(Expression left, Operator operator, Expression right) {
        this("", left, operator, right, "", null, false);
    }

    public Expression(Expression left) {
        this("", left, null, null, "", null, false);

    }

    public Expression() {
        this("", null, null, null, "", null, false);
    }

    // TODO add weight to each Expression used to compare and sort, using the "value" of child Expressions, atomic uses string value

    /**
     * Compared an object with an other object and returns 'true' if they contain the same values
     *
     * @param other The object this is compared to
     * @return If this and the other expressions are the same returns 'true' (regardless or order) otherwise 'false'
     */
    @Override
    public boolean equals(Object other) {

        if (other != null) {
            if (this == other) { // If they are the same object, return true
                return true;
            }
            if (!(other instanceof Expression otherExp)) {
                return false;
            }

            // If both are atomic
            if (isAtomic() && otherExp.isAtomic() && Objects.equals(atomic, otherExp.atomic) && isInverse() == otherExp.isInverse()) {
                return true;
            }
            // If neither is atomic
            else if (operator != null && !(isAtomic() || otherExp.isAtomic()) && operator == otherExp.operator) {

                return Objects.equals(leading, otherExp.leading) && (Objects.equals(left, otherExp.left) &&
                        Objects.equals(right, otherExp.right) || Objects.equals(left, otherExp.right) && Objects.equals(right, otherExp.left));
            }
        }
        return false;
    }

    /**
     * @param other The other expression
     *              example: A & !A => true, A & A => false
     * @return {boolean} Returns 'true' if this and other is the opposite of eachother, otherwise 'false'
     */
    public boolean equalsAndOpposite(@NotNull Expression other) {
        if (StringUtils.numberOfChar(leading, '¬') % 2 == 1) {
            return comparePositiveEquals(other);
        }
        else if (StringUtils.numberOfChar(other.leading, '¬') % 2 == 1) {
            return other.comparePositiveEquals(this);
        }

        return Objects.equals(left, other.left) && Objects.equals(right, other.right) && operator != other.operator &&
                operator != Operator.IMPLICATION && other.operator != Operator.IMPLICATION;
    }

    private boolean comparePositiveEquals(@NotNull Expression other) {
        return new Expression(left, operator, right, atomic).equals(other);
    }

    /**
     * Checks if an expression has changed, if 'true' before, after and the law will be stored in an object and pushed to an array
     *
     * @param exp The expression the method compares to
     * @param law The previously used law
     * @return {string} If the expression is changed, return the new toString() value of it, otherwise return the old
     */
    @NotNull
    public String isChangedThenAdd(@NotNull String exp, @NotNull String law, List<OrderOperations> operations) {
        if (!exp.equals(toString())) {
            OrderOperations op = new OrderOperations(exp, toString(), law);
            operations.add(op);
            exp = toString();
        }
        return exp;
    }

    /**
     * Calls all the laws then checks if the expression has been changed after
     */
    public void laws(List<OrderOperations> operations, Language language) {
        boolean isEnglish = language == Language.ENGLISH;

        String exp = toString();
        eliminationOfImplication();
        exp = isChangedThenAdd(exp, isEnglish ? "Elimination of implication" : "Eliminering av implikasjon", operations);
        doubleNegation();
        exp = isChangedThenAdd(exp, isEnglish ? "Double negation" : "Dobbel negasjon", operations);
        deMorgansLaws();
        exp = isChangedThenAdd(exp, isEnglish ? "De Morgan's Laws" : "De Morgans lover", operations);
        absorptionLaw();
        exp = isChangedThenAdd(exp, isEnglish ? "Absorption law" : "Absorpsjons loven", operations);
        associativeProperty();
        exp = isChangedThenAdd(exp, isEnglish ? "Associative property" : "Assosisative egenskaper", operations);
        distributiveProperty();
        isChangedThenAdd(exp, isEnglish ? "Distributivity" : "Distributivitet", operations);
    }

    /**
     * Removes unnecessary parentheses
     */
    public void removeParenthesis(List<OrderOperations> operations) {

        if (left != null && right != null) {
            final String exp = toString();

            if (operator == Operator.AND && !isInverse() || isAtomic()) {
                removeLeadingAndTrailing();
            }
            // One is atomic, and the other is not
            else if (eitherChildAtomic()) {
                if (operator == left.operator && !left.isInverse()) {
                    left.removeLeadingAndTrailing();
                }
                else if (operator == right.operator && !right.isInverse()) {
                    right.removeLeadingAndTrailing();
                }
            }
            // Neither is atomic
            else if (!eitherChildAtomic()) {
                if (left.operator == right.operator && !left.isInverse() && !right.isInverse()) {
                    left.removeLeadingAndTrailing();
                    right.removeLeadingAndTrailing();
                }
                if (operator == left.operator && !left.isInverse()) {
                    left.removeLeadingAndTrailing();
                }
                else if (operator == right.operator && !right.isInverse()) {
                    right.removeLeadingAndTrailing();
                }
            }
            isChangedThenAdd(exp, "Removal of parentheses", operations);
        }
    }

    /**
     * example: A & B | B & C <=> B & (A | C)
     * example: (A | B) & (B | C) <=> B | A & C
     *
     * @link <a href="https://en.wikipedia.org/wiki/Distributive_property">Wikipedia</a>
     */
    public void distributiveProperty() {

        if (left != null && right != null && !eitherChildAtomic()) {

            // TODO clean up
            if (left.left != null && left.right != null && right.left != null && right.right != null &&
                    left.operator != operator) {

                final String leftLeft = left.left.atomic;
                final String leftRight = left.right.atomic;
                final String rightLeft = right.left.atomic;
                final String rightRight = right.right.atomic;

                if (leftLeft != null && Objects.equals(leftLeft, rightLeft) && !Objects.equals(leftRight, rightRight)) {
                    setObjects(left.right, right.right, left.left);
                }
                else if (leftLeft != null && Objects.equals(leftLeft, rightRight) && !Objects.equals(leftRight, rightLeft)) {
                    setObjects(left.right, right.left, left.left);
                }
                else if (leftRight != null && Objects.equals(leftRight, rightLeft) && !Objects.equals(leftLeft, rightRight)) {
                    setObjects(left.left, right.right, left.right);
                }
                else if (leftRight != null && Objects.equals(leftRight, rightRight) && !Objects.equals(leftLeft, rightLeft)) {
                    setObjects(left.left, right.left, left.right);
                }
            }
        }
    }

    private void setObjects(@NotNull Expression left, @NotNull Expression right, @NotNull Expression common) {
        this.right = new Expression(left, operator, right);
        this.left = new Expression(common);
        this.operator = operator == Operator.AND ? Operator.OR : Operator.AND;

        if (operator != Operator.AND) {
            if (bothChildrenAtomic() && noParentheses()) {
                leading += "(";
                trailing += ")";
            }
        }
        // right.operator == or
        else if (this.right.noParentheses()) {
            this.right.leading = "(";
            this.right.trailing = ")";
        }
    }

    private boolean noParentheses() {
        return !leading.contains("(") || !trailing.contains(")");
    }

    /**
     * example: !A & !B <=> !(A | B) or !(!A | B) <=> A & !B
     *
     * @link <a href="https://en.wikipedia.org/wiki/De_Morgan%27s_laws">Wikipedia</a>
     */
    public void deMorgansLaws() {

        if (left != null && right != null) {
            // Left and right side uses negation
            if (bothChildrenInverse()) {
                Operator newOperator = null;

                if (operator == Operator.AND) {
                    newOperator = Operator.OR;
                }
                else if (operator == Operator.OR) {
                    newOperator = Operator.AND;
                }

                if (newOperator != null) {
                    leading = "¬(";
                    left.removeNot();
                    operator = newOperator;
                    right.removeNot();
                    trailing = ")";
                }
            }
            // The entire expression uses negation
            else if (isInverse()) {

                if (left.isInverse()) {
                    setNot(left, right);
                }
                else if (right.isInverse()) {
                    setNot(right, left);
                }
            }
            // Left side uses negation, but right side does not
            else if (left.isInverse() && !right.isInverse()) {
                left.deMorgansLaws();
            }
        }
    }

    private void flipOperatorAndRemoveLeadingAndTrailing() {
        removeLeadingAndTrailing();
        operator = operator == Operator.AND ? Operator.OR : Operator.AND;

        if (left != null && !left.isAtomic()) {
            left.removeLeadingAndTrailing();
        }
        else if (right != null && !right.isAtomic()) {
            right.removeLeadingAndTrailing();
        }
    }

    private void removeLeadingAndTrailing() {
        leading = "";
        trailing = "";
    }

    private void setNot(@NotNull Expression remove, @NotNull Expression add) {
        flipOperatorAndRemoveLeadingAndTrailing();
        remove.removeNot();
        add.leading = "¬";
        if (!add.isAtomic()) {
            add.leading += "(";
            add.trailing = ")";
        }
    }

    /**
     * Checks if an expression has the not operator before it
     *
     * @return Returns 'true' if it contains 'not', otherwise 'false'
     */
    @JsonIgnore
    public boolean isInverse() {
        return StringUtils.numberOfChar(leading, '¬') % 2 == 1;
    }

    private boolean bothChildrenInverse() {
        return (left != null && left.isInverse()) && right != null && right.isInverse();
    }

    private boolean eitherChildInverse() {
        return left != null && left.isInverse() || right != null && right.isInverse();
    }

    /**
     * Removes a single ¬ operator, with an empty string
     */
    private void removeNot() {
        leading = leading.replace("¬", "");
    }

    /**
     * @link <a href="https://en.wikipedia.org/wiki/Associative_property">Wikipedia</a>
     */
    public void associativeProperty() {
        // TODO?
    }

    /**
     * example: B & A <=> A & B
     *
     * @link <a href="https://en.wikipedia.org/wiki/Commutative_property">Wikipedia</a>
     */
    public void commutativeProperty(List<OrderOperations> operations) {

        if (left != null && bothChildrenAtomic() && left.atomic.compareTo(right.atomic) >= 0) {
            String exp = toString();
            if (!Objects.equals(left.atomic, right.atomic) || left.equalsAndOpposite(right) && !right.isInverse()) {
                swapChildren();
                isChangedThenAdd(exp, "Commutative", operations);
            }
        }
    }

    void swapChildren() {
        Expression help = left;
        left = right;
        right = help;
        log.debug("Left and right child swapped");
    }

    /**
     * example: A -> B <=> !A | B
     */
    public void eliminationOfImplication() {

        if (left != null && right != null && operator == Operator.IMPLICATION) {

            if (!left.isAtomic() && left.noParentheses()) {
                left.leading += "(";
                left.trailing += ")";
            }
            left.leading = "¬" + left.leading;
            operator = Operator.OR;
        }
    }

    /**
     * example: A & (A | B) <=> A or A | (A & B) <=> A
     *
     * @link <a href="https://en.wikipedia.org/wiki/Absorption_law">Wikipedia</a>
     */
    public void absorptionLaw() {

        if (left != null && right != null) {

            // If both are atomic values
            if (bothChildrenAtomic()) {
                if (left.equals(right)) {
                    removeRight();
                }
            }
            else if (eitherChildAtomic()) { // If one is atomic eg: A | (A & B)

                // TODO simplify

                if (left.isAtomic()) {
                    removeRedundant(left, right, false);
                }
                else { // Right is atomic
                    removeRedundant(right, left, true);
                }
            }
            else { // Neither of the expressions are atomic, eg: (A & B) | (A & B)
                if (left.equals(right)) {
                    if (!bothChildrenInverse() || bothChildrenInverse()) {
                        removeRight();
                    }
                    if (!left.isInverse()) {
                        left.removeLeadingAndTrailing();
                    }
                }
                else {
                    if (!left.equalsAndOpposite(right)) {

                        final boolean leftLeftEqRightLeft = Objects.equals(left.left, right.left);
                        final boolean leftRightEqRightRight = Objects.equals(left.right, right.right);
                        final boolean leftLeftEqRightRight = Objects.equals(left.left, right.right);
                        final boolean leftRightEqRightLeft = Objects.equals(left.right, right.left);

                        // Ex: (A | B) | (A & B), remove (A & B)
                        if (left.inverseEqual(right) &&
                                (leftLeftEqRightLeft && leftRightEqRightRight || leftLeftEqRightRight && leftRightEqRightLeft)) {

                            if (left.operator == Operator.AND) {
                                left = right;
                                removeRight();
                            }
                            else if (right.operator == Operator.AND) {
                                removeRight();
                            }
                        }
                        else if (left.operator == operator && right.operator == operator) {
                            // Ex: (A | B) | (A | C) <=> A | B | C
                            if (leftLeftEqRightLeft || leftRightEqRightLeft) {
                                right.left = right.right;
                                right.removeRight();
                            }
                            else if (leftLeftEqRightRight || leftRightEqRightRight) {
                                right.removeRight();
                            }
                        }
                    }
                }
            }
        }
    }

    private void removeRight() {
        if (isInverse() && left != null) {
            left.leading = "¬";
        }
        leading = "";
        operator = null;
        right = null;
        trailing = "";
    }

    private boolean inverseEqual(@NotNull Expression other) {
        return isInverse() == other.isInverse();
    }

    /**
     * TODO Simplify
     */
    private void removeRedundant(@NotNull Expression left, @NotNull Expression right, boolean removeLeft) {

        if (right.left != null && right.right != null) {

            boolean leftEqualsLeft = left.equals(right.left);
            boolean leftEqualsRight = left.equals(right.right);

            // Remove the entire left side
            if (operator == Operator.AND && right.left.equalsAndOpposite(right.right)) {
                if (!removeLeft) {
                    this.left = this.right;
                }
                removeRight();
            }
            // Removed the entire right side
            else if ((operator == Operator.OR && right.operator == Operator.AND ||
                    operator == Operator.AND && right.operator == Operator.OR) &&
                    !right.isInverse() && (leftEqualsLeft || leftEqualsRight)) {
                if (removeLeft) {
                    this.left = this.right;
                }
                removeRight();
            }
            // If right side is always false and operator is "or", remove right side (Ex: B | (A & ¬A) <=> B)
            else if (operator == Operator.OR && right.left.equalsAndOpposite(right.right) && right.operator == Operator.AND) {
                if (removeLeft) {
                    this.left = this.right;
                }
                removeRight();
            }
            // Removes the left side of the right side
            else {
                if (leftEqualsLeft && (operator != Operator.IMPLICATION || right.operator == Operator.AND) &&
                        (!right.isInverse() || right.operator == Operator.OR) ||
                        left.equalsAndOpposite(right.left) && operator == Operator.OR &&
                                right.operator == Operator.AND || left.equalsAndOpposite(right.right)) {
                    right.left = right.right;
                    right.removeRight();
                }
                // Removes the right side of the right side
                else if (leftEqualsRight || leftEqualsLeft && (operator == Operator.IMPLICATION &&
                        right.operator == Operator.OR || !Objects.equals(left.leading, right.leading)) ||
                        left.equalsAndOpposite(right.right) || left.equalsAndOpposite(right.left)) {
                    right.removeRight();
                }
            }
        }
    }

    /**
     * Removes unnesessarry 'not' operators, if there's an even number, removes them completely.
     * If there's an odd number, remove all but one.
     * example: !!A <=> A or !!!A <=> !A
     *
     * @link <a href="https://en.wikipedia.org/wiki/Double_negation">Wikipedia</a>
     */
    public void doubleNegation() {
        int index = 0;
        while (index < leading.length() && leading.charAt(index) == '¬') {
            index++;
        }
        if (index > 1) {
            leading = leading.replace("¬", "");
            if (index % 2 == 1) {
                leading = "¬" + leading;
            }
        }
        // TODO should not be necessarry
        if (left != null) {
            left.doubleNegation();
        }
        if (right != null) {
            right.doubleNegation();
        }
    }

    @JsonIgnore
    public int getNumberOfAtomics() {
        return getNumberOfAtomics(this);
    }

    /**
     * Finds and returns the number of atomic values in the expression
     *
     * @param exp The Expression
     * @return {number} The number of atomic expressions in the expression
     */
    private static int getNumberOfAtomics(@Nullable Expression exp) {
        if (exp == null) {
            return 0;
        }
        else if (exp.isAtomic()) {
            return 1;
        }
        return getNumberOfAtomics(exp.left) + getNumberOfAtomics(exp.right);
    }

    /**
     * Takes in an expression with a true or false value for each side, then calculates the correct truth value
     *
     * @param left  Left side of the expression.
     * @param right right side of the expression.
     * @return If the expression is truthy, returns 'true', otherwise 'false'
     */
    public boolean solve(boolean left, boolean right) {

        boolean result = operator != null ? operator.test(left, right) : left;
        if (isInverse()) {
            result = !result;
        }
        return result;
    }

    @NotNull
    public Expression[] toSetArray() {
        return toSetArray(false);
    }

    /**
     * @return A set of all expressions in the tree structure
     */
    @NotNull
    public Expression[] toSetArray(boolean hideIntermediates) {
        final List<Expression> list = new ArrayList<>();
        toSetArray(this, this, list, hideIntermediates);
        return list.toArray(Expression[]::new);
    }

    /**
     * @param exp         The current object
     * @param expressions An empty list of type Expression, where the objects will be stored
     */
    private static void toSetArray(@Nullable Expression exp, @NotNull Expression root, @NotNull List<Expression> expressions, boolean hideIntermediates) {
        if (exp != null) {
            toSetArray(exp.left, root, expressions, hideIntermediates);
            toSetArray(exp.right, root, expressions, hideIntermediates);

            boolean oppositeExists = false;

            if (hideIntermediates) {
                if (exp != root && !exp.isAtomic()) {
                    return;
                }
            }
            for (Expression ex : expressions) {
                if (exp.equals(ex)) {
                    return;
                }
                else if (exp.equalsAndOpposite(ex)) {
                    oppositeExists = true;
                }
            }

            if (!oppositeExists && StringUtils.numberOfChar(exp.leading, '¬') % 2 == 1) {
                expressions.add(new Expression(exp.left, exp.operator, exp.right, exp.atomic));
            }
            expressions.add(exp);
        }
    }

    /**
     * Checks if this expression is an atomic value
     *
     * @return Returns 'true' if the expression is atomic
     */
    public boolean isAtomic() {
        return this.atomic != null;
    }

    public boolean bothChildrenAtomic() {
        return left != null && left.isAtomic() && right != null && right.isAtomic();
    }

    public boolean eitherChildAtomic() {
        return left != null && left.isAtomic() || right != null && right.isAtomic();
    }

    @NotNull
    public String getLeading() {
        return leading;
    }

    public void setLeading(@NotNull String leading) {
        this.leading = leading;
    }

    public void appendLeading(String leading) {
        this.leading += leading;
    }

    @Nullable
    public Expression getLeft() {
        return left;
    }

    public void setLeft(@Nullable Expression left) {
        this.left = left;
    }

    @Nullable
    public Operator getOperator() {
        return operator;
    }

    public void setOperator(@Nullable Operator operator) {
        this.operator = operator;
    }

    @Nullable
    public Expression getRight() {
        return right;
    }

    public void setRight(@Nullable Expression right) {
        this.right = right;
    }

    @NotNull
    public String getTrailing() {
        return trailing;
    }

    public void setTrailing(@NotNull String trailing) {
        this.trailing = trailing;
    }

    public void appendTrailing(String trailing) {
        this.trailing = trailing;
    }

    public String getAtomic() {
        return atomic;
    }

    public void setAtomic(@Nullable String atomic) {
        this.atomic = atomic;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    /**
     * Returns a string representation of the expression
     * example: A & B | (¬C -> D)
     *
     * @return {string} A string representation of the expression
     */
    @NotNull
    @Override
    public String toString() {
        if (isAtomic()) {
            return leading + (!caseSensitive ? StringUtils.capitalizeFirstLetter(atomic) : atomic);
        }
        StringBuilder s = new StringBuilder(leading);
        if (left != null) {
            s.append(left);
        }
        if (operator != null) {
            s.append(" ").append(operator).append(" ");
        }
        if (right != null) {
            s.append(right);
        }
        s.append(trailing);
        return s.toString();
    }
}
