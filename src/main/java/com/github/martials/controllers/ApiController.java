package com.github.martials.controllers;

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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@CrossOrigin
@RestController
@Tag(name = "Simplify", description = "Simplify Truth-values and generate truth tables.")
public final class ApiController { // TODO all params, body and headers are shown as required, even if they are not

    private static final Logger log = LoggerFactory.getLogger(ApiController.class);

    /**
     * @param exp      A truth expression
     * @param lang     Overrides the language in the header
     * @param header   The accept language section of the header, the prefered language will be used, unless english is set
     * @param simplify Wheter or not to simplify the given expression
     * @return The result of the simplified expression, or null if not valid
     */
    @NotNull
    @Operation(
            summary = "Simplify a truth expression",
            description = "Simplify a truth expression, and return the result. If the expression is not valid, the result will be empty with an error message.",
            tags = {"Simplify"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The expression was valid and simplified", content = {@Content(schema = @Schema(implementation = Result.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "The expression was not valid", content = {@Content(schema = @Schema(implementation = EmptyResult.class), mediaType = "html/text")}),
    })
    @GetMapping("/simplify/{exp}")
    public ResponseEntity<EmptyResult> simplify(@PathVariable @NotNull final String exp,
                                                @RequestParam(required = false) @Nullable final String lang,
                                                @RequestParam(defaultValue = "true") final boolean simplify,
                                                @RequestParam(defaultValue = "false") final boolean caseSensitive,
                                                @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "nb") final String header) {

        log.info("Simplify call with the following parametres: exp=" + exp + ", lang=" + lang + ", simplify=" + simplify,
                ", caseSensitive=" + caseSensitive);

        final ExpressionUtils eu = initiate(exp, lang, simplify, caseSensitive, header);

        final long startTime = System.currentTimeMillis();
        final ResponseEntity<EmptyResult> result = simplifyIfLegal(eu, expression -> new Result(exp, expression.toString(), eu.getOperations(), expression));
        log.info("Expression simplified in: " + (System.currentTimeMillis() - startTime) + "ms");

        log.debug("Result sent: {}", result);
        return result;
    }

    /**
     * @return A matrix representation of a table with truth values
     */
    @NotNull
    @Operation(
            summary = "Generate a truth table",
            description = "Generate a truth table, and return the result. If the expression is not valid, the result will be empty with an error message.",
            tags = {"table"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The expression was valid and a table was generated", content = {@Content(schema = @Schema(implementation = ResultOnlyTable.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "The expression was not valid", content = {@Content(schema = @Schema(implementation = EmptyResult.class), mediaType = "html/text")}),
            @ApiResponse(responseCode = "404", description = "The body was empty", content = {@Content(schema = @Schema(implementation = EmptyResult.class), mediaType = "html/text")}),
    })
    @PostMapping("/table")
    public ResponseEntity<EmptyResult> table(@RequestBody(required = false) @Nullable final Expression exp,
                                             @RequestHeader(defaultValue = "DEFAULT") final Sort sort,
                                             @RequestHeader(defaultValue = "NONE") final Hide hide,
                                             @RequestHeader(defaultValue = "false") final boolean hideIntermediate,
                                             @RequestHeader(required = false) @Nullable final String lang,
                                             @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "nb") final String header) {

        log.info("Table call with the following parametres: exp={}, sort={}, hide={}, hideIntermediate={}, lang={}", exp, sort, hide, hideIntermediate, lang);

        setAndLogLanguage(lang, header);

        if (exp == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Expression not found in body");
        }
        try {
            new ExpressionUtils(exp.toString().replace(" ", "")).isLegalExpression();
        }
        catch (IllegalCharacterException | MissingCharacterException | TooBigExpressionException e) {
            log.debug(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        final TruthTable table = new TruthTable(exp.toSetArray(hideIntermediate));
        log.debug("New table created: {}", table);

        final ResponseEntity<EmptyResult> result = new ResponseEntity<>(new ResultOnlyTable(exp.toString(), mapToStrings(table), table), HttpStatus.OK);
        log.debug("Result sent: {}", result);

        return result;
    }

    /**
     * @return A simplified expression and a matrix representation of a table with truth values
     */
    @NotNull
    @Operation(
            summary = "Simplify a truth expression and generate a truth table",
            description = "Simplify a truth expression, and return the result. If the expression is not valid, the result will be empty with an error message.",
            tags = {"Simplify", "table"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The expression was valid and a table was generated", content = {@Content(schema = @Schema(implementation = ResultWithTable.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "The expression was not valid", content = {@Content(schema = @Schema(implementation = EmptyResult.class), mediaType = "html/text")}),
    })
    @GetMapping("/simplify/table/{exp}")
    public ResponseEntity<EmptyResult> simplifyAndTable(@PathVariable @NotNull final String exp,
                                                        @RequestParam(required = false) @Nullable final String lang,
                                                        @RequestParam(defaultValue = "true") final boolean simplify,
                                                        @RequestParam(defaultValue = "false") final boolean caseSensitive,
                                                        @RequestParam(defaultValue = "DEFAULT") final Sort sort,
                                                        @RequestParam(defaultValue = "NONE") final Hide hide,
                                                        @RequestParam(defaultValue = "false") final boolean hideIntermediate,
                                                        @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "nb") @NotNull final String header) {

        log.info("Simplify and table call with the following parametres: exp=" + exp + ", lang=" + lang +
                ", simplify=" + simplify + ", sort=" + sort + ", hide=" + hide + ", hideIntermediate=" + hideIntermediate + ", caseSensitive=" + caseSensitive);

        final ExpressionUtils eu = initiate(exp, lang, simplify, caseSensitive, header);

        final long startTime = System.currentTimeMillis();
        final ResponseEntity<EmptyResult> result = simplifyIfLegal(eu, expression -> {

            TruthTable table = new TruthTable(expression.toSetArray(hideIntermediate), hide, sort);
            log.debug("New table created: {}", table);

            return new ResultWithTable(exp, expression.toString(), eu.getOperations(),
                    expression, mapToStrings(table), table);
        });

        log.info("Expression simplified in: " + (System.currentTimeMillis() - startTime) + "ms");

        log.debug("Result sent: {}", result);
        return result;
    }

    @NotNull
    private ExpressionUtils initiate(@NotNull String exp, String lang, boolean simplify, boolean caseSensitive, String header) {
        Language language = setAndLogLanguage(lang, header);

        final String newExpression = replace(exp, caseSensitive);
        return new ExpressionUtils(newExpression, simplify, language, caseSensitive);
    }

    @Nullable
    private String[] mapToStrings(TruthTable table) {
        if (table == null) {
            return null;
        }
        return Arrays.stream(table.getExpressions())
                .map(Expression::toString)
                .toArray(String[]::new);
    }

    @NotNull
    private ResponseEntity<EmptyResult> simplifyIfLegal(@NotNull ExpressionUtils eu, Function<Expression, EmptyResult> function) {

        try {
            eu.isLegalExpression();
        }
        catch (IllegalCharacterException | MissingCharacterException | TooBigExpressionException e) {
            log.debug(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        final Expression expression;

        expression = eu.simplify();
        log.debug("Expression simplified to: {}", expression);

        return ResponseEntity.ok(function.apply(expression));
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
