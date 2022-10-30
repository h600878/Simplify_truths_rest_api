package com.github.martials;

public enum Language {
    norwegianBokmaal("nb"),
    english("en");

    private final String lang;

    Language(String lang) {
        this.lang = lang;
    }

    public String getLang() {
        return lang;
    }
}
