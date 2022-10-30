package com.github.martials;

import com.github.martials.classes.Expression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Result(@NotNull String status, @NotNull String before, @NotNull String after, @Nullable Expression expression) {

}
