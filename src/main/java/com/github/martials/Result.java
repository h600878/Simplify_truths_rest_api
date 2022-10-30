package com.github.martials;

import com.github.martials.expressions.Expression;
import com.github.martials.expressions.OrderOperations;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record Result(@NotNull Status status, @NotNull String before, @NotNull String after,
                     @NotNull List<OrderOperations> orderOperations, @Nullable Expression expression) {

}
