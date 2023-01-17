package com.github.martials.exceptions;

import com.github.martials.enums.Language;

public class MissingCharacterException extends RuntimeException {

    private static final String englishMessage = "Missing character";
    private static final String norwegianMessage = "Mangler tegn";

    public MissingCharacterException(Language language, char c) {
        super((language == Language.ENGLISH ? englishMessage : norwegianMessage) + " '" + c + "'");
    }

    public MissingCharacterException() {
        super(englishMessage);
    }

}
