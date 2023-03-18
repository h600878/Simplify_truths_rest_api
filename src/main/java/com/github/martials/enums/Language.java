package com.github.martials.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Language", description = "Language of the result")
public enum Language {
    NORWEGIAN_BOKMAAL("nb"),
    ENGLISH("en");

    private final String lang;

    Language(String lang) {
        this.lang = lang;
    }

    public String getLang() {
        return lang;
    }
}
