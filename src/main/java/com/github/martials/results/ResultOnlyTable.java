package com.github.martials.results;

import com.github.martials.Status;
import com.github.martials.expressions.TruthTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class ResultOnlyTable extends EmptyResult {

    @NotNull
    protected final String expression;
    protected final String[] header;
    @Nullable
    protected final TruthTable table;

    public ResultOnlyTable(@NotNull Status status, @NotNull String expression, @Nullable String[] header, @Nullable TruthTable table) {
        super(status);
        this.expression = expression;
        this.header = header;
        this.table = table;
    }

    @NotNull
    public String getExpression() {
        return expression;
    }

    @Nullable
    public String[] getHeader() {
        return header;
    }

    @Nullable
    public TruthTable getTable() {
        return table;
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
