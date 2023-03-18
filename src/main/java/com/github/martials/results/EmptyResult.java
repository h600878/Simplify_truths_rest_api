package com.github.martials.results;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jetbrains.annotations.NotNull;

@Schema(name = "EmptyResult", description = "Empty result with a status")
public abstract class EmptyResult {

    @NotNull
    protected final String status;

    public EmptyResult() {
        this.status = "OK";
    }

    @NotNull
    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "EmptyResult{" +
                "status=" + status +
                '}';
    }
}
