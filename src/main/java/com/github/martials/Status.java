package com.github.martials;

import org.jetbrains.annotations.NotNull;

// TODO change to enum
public record Status(int code, @NotNull String message) {

    public static final Status OK, ILLEGAL_CHARACTER, MISSING_CHARACTER;

    static {
        boolean isEnglish = SimplifyTruthsRestApiApplication.lang == Language.english;

        OK = new Status(200, "Ok");
        ILLEGAL_CHARACTER = new Status(501, isEnglish ? "Illegal character" : "Ugyldig tegn");
        MISSING_CHARACTER = new Status(502, isEnglish ? "Missing character" : "Manglende tegn");
    }
}
