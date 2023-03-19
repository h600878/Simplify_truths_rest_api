package com.github.martials.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Schema(name = "Language", description = "Language of the result")
public enum Language {
    NORWEGIAN_BOKMAAL("nb"),
    ENGLISH("en");

    private final String lang;

    private static final Logger log = LoggerFactory.getLogger(Language.class);
    private static final List<String> NOR_LANGS = List.of("nb", "no", "nn");

    Language(String lang) {
        this.lang = lang;
    }

    public String getLang() {
        return lang;
    }

    @NotNull
    public static Language setLanguage(@Nullable String lang, @NotNull String header) {
        log.info("ACCEPT_LANGUAGE header=" + header);
        Language language = setLanguageRec(lang, header);
        log.info("Language set to " + language);
        return language;
    }

    @NotNull
    private static Language setLanguageRec(@Nullable String lang, @NotNull String header) {
        final String headerLang = header.substring(0, 2);

        if (lang != null) {
            for (Language language : Language.values()) {
                if (lang.equalsIgnoreCase(language.getLang())) {
                    return language;
                }
            }
            log.warn("Language was not found");
        }
        else if (headerLang.equalsIgnoreCase("en")) {
            return Language.ENGLISH;
        }
        else if (!NOR_LANGS.contains(headerLang.toLowerCase())) { // If neither "en", or the Norwegian languages
            try {
                setLanguageRec(null, header.split(",")[1]);
            }
            catch (IndexOutOfBoundsException e) {
                log.warn("No language recognized in ACCEPT_LANGUAGE");
                log.debug("Exception: ", e);
            }
        }
        return Language.NORWEGIAN_BOKMAAL; // Default
    }
}
