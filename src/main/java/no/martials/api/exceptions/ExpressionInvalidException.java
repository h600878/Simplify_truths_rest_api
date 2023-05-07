package no.martials.api.exceptions;

import no.martials.api.enums.Language;

import java.util.List;
import java.util.Map;

public class ExpressionInvalidException extends RuntimeException {

    private static final String englishMessage = "Illegal character(s)";

    private static final Map<Language, List<String>> mapOfMessages = Map.of(
            Language.ENGLISH, List.of(englishMessage, " at index "),
            Language.NORWEGIAN_BOKMAAL, List.of("Ulovlig karakter(er)", " ved indeks ")
    );

    public ExpressionInvalidException(Language language, String c, int index) {
        super(mapOfMessages.get(language).get(0) + " '" + c + "'," + mapOfMessages.get(language).get(1) + index);
    }

    public ExpressionInvalidException() {
        super(englishMessage);
    }
}
