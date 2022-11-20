package com.github.martials;

import org.jetbrains.annotations.NotNull;

public record Status(int code, @NotNull String message) {

    public static final Status OK, NOT_FOUND;

    static {
        OK = new Status(200, "Ok");
        NOT_FOUND = new Status(404, "Expression not found");
    }
}
