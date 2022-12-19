package com.github.martials.exceptions;

import com.github.martials.enums.Language;

public class IllegalCharacterException extends RuntimeException {

    public IllegalCharacterException(Language language, char c) {
        super((language == Language.english ? "Illegal character" : "Ugyldig tegn") + " '" + c + "'");
    }

}
