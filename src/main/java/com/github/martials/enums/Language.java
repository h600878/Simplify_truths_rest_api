package com.github.martials.enums;

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
