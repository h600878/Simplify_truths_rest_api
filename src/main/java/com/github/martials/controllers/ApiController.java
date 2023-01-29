package com.github.martials.controllers;

import com.github.martials.Status;
import com.github.martials.enums.Hide;
import com.github.martials.enums.Language;
import com.github.martials.enums.Sort;
import com.github.martials.exceptions.IllegalCharacterException;
import com.github.martials.exceptions.MissingCharacterException;
import com.github.martials.exceptions.TooBigExpressionException;
import com.github.martials.expressions.Expression;
import com.github.martials.expressions.TruthTable;
import com.github.martials.results.EmptyResult;
import com.github.martials.results.Result;
import com.github.martials.results.ResultOnlyTable;
import com.github.martials.results.ResultWithTable;
import com.github.martials.utils.ExpressionUtils;
import com.github.martials.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

// TODO allow all origins?
@RestController
@CrossOrigin(origins = {"http://localhost:8000", "http://localhost:3000", "https://martials.no/", "https://h600878.github.io/", "https://api.martials.no"})
public class ApiController { // TODO make sure it's thread-safe

    private static final Logger log = LoggerFactory.getLogger(ApiController.class);

    /**
     * @param exp      A truth expression
     * @param lang     Overrides the language in the header
     * @param header   The accept language section of the header, the prefered language will be used, unless english is set
     * @param simplify Wheter or not to simplify the given expression
     * @return The result of the simplified expression, or null if not valid
     */
    @NotNull
    @GetMapping("/simplify")
    public EmptyResult simplify(@RequestParam(required = false) @Nullable final String exp,
                                @RequestParam(required = false) @Nullable final String lang,
                                @RequestParam(defaultValue = "true") final boolean simplify,
                                @RequestParam(defaultValue = "false") final boolean caseSensitive,
                                @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "nb") final String header) {

        log.info("Simplify call with the following parametres: exp=" + exp + ", lang=" + lang + ", simplify=" + simplify,
                ", caseSensitive=" + caseSensitive);

        final ExpressionUtils eu = initiate(exp, lang, simplify, caseSensitive, header);
        if (eu == null) {
            return new EmptyResult(Status.NOT_FOUND);
        }
        assert exp != null;

        final long startTime = System.currentTimeMillis();
        final EmptyResult result = simplifyIfLegal(eu, expression -> new Result(Status.OK, exp, expression.toString(), eu.getOperations(), expression));
        log.info("Expression simplified in: " + (System.currentTimeMillis() - startTime) + "ms");

        log.debug("Result sent: {}", result);
        return result;
    }

    /**
     * @return A matrix representation of a table with truth values
     */
    @NotNull
    @PostMapping("/table")
    public EmptyResult table(@RequestBody(required = false) @Nullable final Expression exp,
                             @RequestHeader(defaultValue = "DEFAULT") final Sort sort,
                             @RequestHeader(defaultValue = "NONE") final Hide hide,
                             @RequestHeader(required = false) @Nullable final String lang,
                             @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "nb") final String header) {

        log.info("Table call with the following parametres: exp={}, sort={}, hide={}, lang={}", exp, sort, hide, lang);

        setAndLogLanguage(lang, header);

        if (exp == null) {
            log.warn("Body is empty, exiting...");
            return new EmptyResult(Status.NOT_FOUND);
        }
        try {
            new ExpressionUtils(exp.toString().replace(" ", "")).isLegalExpression();
        }
        catch (IllegalCharacterException | MissingCharacterException | TooBigExpressionException e) {
            log.warn("Expression is not legal, exiting...");
            log.debug(Arrays.toString(e.getStackTrace()));
            return new EmptyResult(new Status(500, e.getMessage()));
        }

        final TruthTable table = new TruthTable(exp.toSetArray());
        log.debug("New table created: {}", table);

        final ResultOnlyTable result = new ResultOnlyTable(Status.OK, exp.toString(), mapToStrings(table), table);
        log.debug("Result sent: {}", result);

        return result;
    }

    /**
     * @return A simplified expression and a matrix representation of a table with truth values
     */
    @NotNull
    @GetMapping("/simplify/table")
    public EmptyResult simplifyAndTable(@RequestParam(required = false) @Nullable final String exp,
                                        @RequestParam(required = false) @Nullable final String lang,
                                        @RequestParam(defaultValue = "true") final boolean simplify,
                                        @RequestParam(defaultValue = "false") final boolean caseSensitive,
                                        @RequestParam(defaultValue = "DEFAULT") final Sort sort,
                                        @RequestParam(defaultValue = "NONE") final Hide hide,
                                        @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "nb") @NotNull final String header) {

        log.info("Simplify and table call with the following parametres: exp=" + exp + ", lang=" + lang +
                ", simplify=" + simplify + ", sort=" + sort + ", hide=" + hide, ", caseSensitive=" + caseSensitive);

        final ExpressionUtils eu = initiate(exp, lang, simplify, caseSensitive, header);
        if (eu == null) {
            return new EmptyResult(Status.NOT_FOUND);
        }

        final long startTime = System.currentTimeMillis();
        final EmptyResult result = simplifyIfLegal(eu, expression -> {

            TruthTable table = new TruthTable(expression.toSetArray(), hide, sort);
            log.debug("New table created: {}", table);
            assert exp != null;
            return new ResultWithTable(Status.OK, exp, expression.toString(), eu.getOperations(),
                    expression, mapToStrings(table), table);
        });

        log.info("Expression simplified in: " + (System.currentTimeMillis() - startTime) + "ms");

        log.debug("Result sent: {}", result);
        return result;
    }

    private ExpressionUtils initiate(String exp, String lang, boolean simplify, boolean caseSensitive, String header) {
        Language language = setAndLogLanguage(lang, header);

        if (exp == null) {
            log.warn("Parametre exp is empty, exiting...");
            return null;
        }

        final String newExpression = replace(exp, caseSensitive);
        return new ExpressionUtils(newExpression, simplify, language, caseSensitive);
    }

    private String[] mapToStrings(TruthTable table) {
        if (table == null) {
            return null;
        }
        return Arrays.stream(table.getExpressions())
                .map(Expression::toString)
                .toArray(String[]::new);
    }

    @NotNull
    private EmptyResult simplifyIfLegal(@NotNull ExpressionUtils eu, Function<Expression, EmptyResult> function) {

        String isLegal = "";

        try {
            eu.isLegalExpression();
        }
        catch (IllegalCharacterException | MissingCharacterException | TooBigExpressionException e) {
            log.debug(Arrays.toString(e.getStackTrace()));
            isLegal = e.getMessage();
        }

        final Expression expression;
        if (isLegal.equals("")) {
            expression = eu.simplify();
            log.debug("Expression simplified to: {}", expression);
        }
        else {
            log.error("Expression is not legal: {}", isLegal);
            expression = null;
        }

        final EmptyResult result;
        if (expression == null) {
            result = new EmptyResult(new Status(500, isLegal));
        }
        else {
            result = function.apply(expression);
        }

        return result;
    }

    @NotNull
    private Language setAndLogLanguage(@Nullable String lang, @NotNull String header) {
        log.info("ACCEPT_LANGUAGE header=" + header);
        Language language = setLanguage(lang, header);
        log.info("Language set to " + language);
        return language;
    }

    @NotNull
    private Language setLanguage(@Nullable String language, @NotNull String header) {
        final String headerLang = header.substring(0, 2);
        final List<String> norLangs = List.of("nb", "no", "nn");

        if (language != null) {
            for (Language lang : Language.values()) {
                if (language.equalsIgnoreCase(lang.getLang())) {
                    return lang;
                }
            }
            log.warn("Language was not found");
        }
        else if (headerLang.equalsIgnoreCase("en")) {
            return Language.ENGLISH;
        }
        else if (!norLangs.contains(headerLang.toLowerCase())) { // If neither "en", or the Norwegian languages
            try {
                setLanguage(null, header.split(",")[1]);
            }
            catch (IndexOutOfBoundsException ignored) {
                log.warn("No language recognized in ACCEPT_LANGUAGE");
            }
        }
        return Language.NORWEGIAN_BOKMAAL; // Default
    }

    @NotNull
    private String replace(@NotNull String expression, boolean caseSensitive) {
        if (!caseSensitive) {
            expression = expression.toLowerCase();
            log.debug("Expression converted to lowercase: {}", expression);
        }

        expression = StringUtils.replaceOperators(expression);
        log.debug("Expression changed to: {}", expression);
        return expression;
    }

}
