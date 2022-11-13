package com.github.martials.results;

import com.github.martials.Status;
import com.github.martials.expressions.Expression;
import com.github.martials.expressions.OrderOperations;
import com.github.martials.expressions.TruthTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ResultWithTable(@NotNull Status status, @NotNull String before, @NotNull String after,
                              @Nullable List<OrderOperations> orderOperations, @Nullable Expression expression,
                              @Nullable String[] header, @Nullable TruthTable table) {
}
