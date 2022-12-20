package com.github.martials.exceptions;

import com.github.martials.enums.Language;

public class TooBigExpressionException extends RuntimeException {

    private static final String englishMessage = "Expression is too big, max 10 arguments";
    private static final String norwegianMessage = "Uttrykket er for stort, maks 10 argumenter";

    public TooBigExpressionException(Language language) {
        super(language == Language.ENGLISH ? englishMessage : norwegianMessage);
    }

    public TooBigExpressionException() {
        super(englishMessage);
    }

}
