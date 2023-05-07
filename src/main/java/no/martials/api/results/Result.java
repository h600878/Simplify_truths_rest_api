package no.martials.api.results;

import no.martials.api.expressions.Expression;
import no.martials.api.expressions.OrderOperations;
import jakarta.validation.constraints.NotBlank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Result extends EmptyResult {

    @NotBlank
    @NotNull
    protected final String before;
    @NotBlank
    @NotNull
    protected final String after;
    @NotBlank
    @Nullable
    protected final List<OrderOperations> orderOperations;
    @NotBlank
    @Nullable
    protected final Expression expression;

    public Result(String version, @NotNull String before, @NotNull String after,
                  @Nullable List<OrderOperations> orderOperations, @Nullable Expression expression) {
        super(version);
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
