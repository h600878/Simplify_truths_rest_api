package com.github.martials.results;

import com.github.martials.expressions.Expression;
import com.github.martials.expressions.OrderOperations;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Result extends EmptyResult {

    @NotNull
    protected final String before;
    @NotNull
    protected final String after;
    @Nullable
    protected final List<OrderOperations> orderOperations;
    @Nullable
    protected final Expression expression;

    public Result(@NotNull String before, @NotNull String after,
                  @Nullable List<OrderOperations> orderOperations, @Nullable Expression expression) {
        super();
        this.before = before;
        this.after = after;
        this.orderOperations = orderOperations;
        this.expression = expression;
    }

    @NotNull
    public String getBefore() {
        return before;
    }

    @NotNull
    public String getAfter() {
        return after;
    }

    @Nullable
    public List<OrderOperations> getOrderOperations() {
        return orderOperations;
    }

    @Nullable
    public Expression getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return "Result{" +
                "before='" + before + '\'' +
                ", after='" + after + '\'' +
                ", orderOperations=" + orderOperations +
                ", expression=" + expression +
                "} " + super.toString();
    }
}
