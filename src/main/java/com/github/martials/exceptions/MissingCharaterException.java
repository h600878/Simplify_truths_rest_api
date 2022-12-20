package com.github.martials.exceptions;

import com.github.martials.enums.Language;

public class MissingCharaterException extends RuntimeException {

    private static final String englishMessage = "Missing character";
    private static final String norwegianMessage = "Mangler tegn";

    public MissingCharaterException(Language language, char c) {
        super((language == Language.english ? englishMessage : norwegianMessage) + " '" + c + "'");
    }

    public MissingCharaterException() {
        super(englishMessage);
    }

}
