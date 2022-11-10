package com.github.martials.results;

import com.github.martials.Status;
import com.github.martials.expressions.TruthTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ResultOnlyTable(@NotNull Status status, @NotNull String expression, @Nullable TruthTable table) {
}
