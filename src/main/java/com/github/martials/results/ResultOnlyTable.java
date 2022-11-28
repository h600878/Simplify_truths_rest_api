package com.github.martials.results;

import com.github.martials.Status;
import com.github.martials.expressions.TruthTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResultOnlyTable extends EmptyResult {

    @NotNull
    protected final String expression;

    @Nullable
    protected final TruthTable table;

    public ResultOnlyTable(@NotNull Status status, @NotNull String expression, @Nullable TruthTable table) {
        super(status);
        this.expression = expression;
        this.table = table;
    }

    @NotNull
    public String getExpression() {
        return expression;
    }

    @Nullable
    public TruthTable getTable() {
        return table;
    }
}
