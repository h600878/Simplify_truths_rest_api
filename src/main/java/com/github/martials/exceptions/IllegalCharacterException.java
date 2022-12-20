package com.github.martials.exceptions;

import com.github.martials.enums.Language;

public class IllegalCharacterException extends RuntimeException {

    private static final String englishMessage = "Illegal character";
    private static final String norwegianMessage = "Ugyldig tegn";

    public IllegalCharacterException(Language language, char c) {
        super((language == Language.english ? englishMessage : norwegianMessage) + " '" + c + "'");
    }

    public IllegalCharacterException() {
        super(englishMessage);
    }
}
