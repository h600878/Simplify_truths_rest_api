package no.martials.api.exceptions;

import no.martials.api.enums.Language;

public class TooBigExpressionException extends RuntimeException {

    private static final String englishMessage = "Expression is too big, max 15 arguments";
    private static final String norwegianMessage = "Uttrykket er for stort, maks 15 argumenter";

    public TooBigExpressionException(Language language) {
        super(language == Language.ENGLISH ? englishMessage : norwegianMessage);
    }

    public TooBigExpressionException() {
        super(englishMessage);
    }

}
