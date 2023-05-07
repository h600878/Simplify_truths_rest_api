package no.martials.api.exceptions;

import no.martials.api.enums.Language;

import java.util.List;
import java.util.Map;

public class MissingCharacterException extends RuntimeException {

    private static final String englishMessage = "Missing character";

    private static final Map<Language, List<String>> mapOfMessages = Map.of(
            Language.ENGLISH, List.of(englishMessage, " at index "),
            Language.NORWEGIAN_BOKMAAL, List.of("Mangler tegn", " ved indeks ")
    );

    public MissingCharacterException(Language language, char c, int index) {
        super(mapOfMessages.get(language).get(0) + " '" + c + "', " + mapOfMessages.get(language).get(1) + index);
    }

    public MissingCharacterException() {
        super(englishMessage);
    }

}
