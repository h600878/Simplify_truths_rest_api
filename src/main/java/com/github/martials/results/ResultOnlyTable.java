package com.github.martials.results;

import com.github.martials.Status;
import com.github.martials.expressions.TruthTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class ResultOnlyTable extends EmptyResult {

    @NotNull
    protected final String expression;

    @Nullable
    protected final TruthTable table;

    protected final String[] header;

    public ResultOnlyTable(@NotNull Status status, @NotNull String expression, @Nullable String[] header, @Nullable TruthTable table) {
        super(status);
        this.expression = expression;
        this.table = table;
        this.header = header;
    }

    @NotNull
    public String getExpression() {
        return expression;
    }

    @Nullable
    public TruthTable getTable() {
        return table;
    }

    @Nullable
    public String[] getHeader() {
        return header;
    }

    @Override
    public String toString() {
        return "ResultOnlyTable{" +
                "expression='" + expression + '\'' +
                ", header=" + Arrays.toString(header) +
                ", table=" + table +
                "} " + super.toString();
    }
}
