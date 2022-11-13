package com.github.martials.results;

import com.github.martials.Status;
import org.jetbrains.annotations.NotNull;

public class EmptyResult {

    @NotNull
    protected final Status status;

    public EmptyResult(@NotNull Status status) {
        this.status = status;
    }

    @NotNull
    public Status getStatus() {
        return status;
    }
}
