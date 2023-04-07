package com.github.martials.results;

import com.github.martials.expressions.Expression;
import com.github.martials.expressions.OrderOperations;
import com.github.martials.expressions.TruthTable;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@Schema(name = "ResultWithTable", description = "Result with a simplified expression and table")
public class ResultWithTable extends Result {

    @Nullable
    private final String[] header;
    @Nullable
    private final TruthTable table;

    public ResultWithTable(String version, @NotNull String before, @NotNull String after,
                           @Nullable List<OrderOperations> orderOperations, @Nullable Expression expression,
                           @Nullable String[] header, @Nullable TruthTable table) {
        super(version, before, after, orderOperations, expression);
        this.header = header;
        this.table = table;
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
        return "ResultWithTable{" +
                "header=" + Arrays.toString(header) +
                ", table=" + table +
                "} " + super.toString();
    }
}
