package com.github.martials.exceptions;

import com.github.martials.enums.Language;

public class MissingCharaterException extends RuntimeException {

    public MissingCharaterException(Language language, char c) {
        super(language == Language.english ? "Missing character" : "Manglende tegn" + " '" + c + "'");
    }

}
