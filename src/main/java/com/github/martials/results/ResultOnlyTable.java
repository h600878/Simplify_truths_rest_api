package com.github.martials.results;

import com.github.martials.expressions.TruthTable;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@Schema(name = "ResultOnlyTable", description = "Result with only the table table")
public class ResultOnlyTable extends EmptyResult {

    @NotNull
    protected final String expression;
    protected final String[] header;
    @Nullable
    protected final TruthTable table;

    public ResultOnlyTable(@NotNull String expression, @Nullable String[] header, @Nullable TruthTable table) {
        super();
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
