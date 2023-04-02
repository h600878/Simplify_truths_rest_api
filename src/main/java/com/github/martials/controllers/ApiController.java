package com.github.martials.controllers;

import com.github.martials.enums.Hide;
import com.github.martials.enums.Language;
import com.github.martials.enums.Sort;
import com.github.martials.exceptions.ExpressionInvalidException;
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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
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
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
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
     * @throws ResponseStatusException If the expression is not valid
     */
    @NotNull
    @Operation(
            summary = "Simplify a truth expression",
            description = "Simplify a truth expression, and return the result." +
                    " If the expression is not valid, the result will be empty with an error message.",
            tags = {"Simplify"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The expression was valid and simplified",
                    content = {@Content(schema = @Schema(implementation = Result.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "The expression was not valid",
                    content = {@Content(schema = @Schema(implementation = EmptyResult.class), mediaType = "html/text")}),
    })
    @Parameters(value = {
            @Parameter(name = "exp", description = "A logical expression"),
            @Parameter(name = "lang", description = "Overrides the language in the header"),
            @Parameter(name = "simplify", description = "Whether or not to simplify the given expression"),
            @Parameter(name = "caseSensitive", description = "Wheter or not to use case sensitive variables"),
    })
    @GetMapping("/simplify/{exp}")
    public ResponseEntity<EmptyResult> simplify(
            @PathVariable @NotNull final String exp,
            @RequestParam(required = false) @Nullable final String lang,
            @RequestParam(defaultValue = "true") final boolean simplify,
            @RequestParam(defaultValue = "false") final boolean caseSensitive,
            @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "nb") final String header) {

        log.info("Simplify call with the following parametres: exp=" + exp + ", lang=" + lang + ", simplify=" + simplify,
                ", caseSensitive=" + caseSensitive);

        StopWatch sw = new StopWatch();
        final ExpressionUtils eu = getExpressionUtils(exp, lang, header, simplify, caseSensitive, sw);

        final ResponseEntity<EmptyResult> result = simplify(eu, expression ->
                new Result(exp, expression.toString(), eu.getOperations(), expression));

        if (log.isDebugEnabled()) {
            sw.stop();
            log.debug("Expression simplified in: " + sw.getTotalTimeMillis() + "ms");
        }

        log.debug("Result sent: {}", result);
        return result;
    }

    /**
     * @return A matrix representation of a table with truth values
     * @throws ResponseStatusException If the expression is not valid
     */
    @NotNull
    @Operation(
            summary = "Generate a truth table",
            description = "Generate a truth table, and return the result." +
                    " If the expression is not valid, the result will be empty with an error message.",
            tags = {"table"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The expression was valid and a table was generated",
                    content = {@Content(schema = @Schema(implementation = ResultOnlyTable.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "The expression was not valid",
                    content = {@Content(schema = @Schema(implementation = EmptyResult.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "The body was empty",
                    content = {@Content(schema = @Schema(implementation = EmptyResult.class), mediaType = "html/text")}),
    })
    @Parameters(value = {
            @Parameter(name = "lang", description = "Overrides the language in the header"),
            @Parameter(name = "sort", description = "Sort the variables in the table"),
            @Parameter(name = "hide", description = "Hide the variables in the table"),
            @Parameter(name = "hideIntermediate", description = "Hide the intermediate steps in the table"),
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "A logical expression of type Expression",
            content = @Content(schema = @Schema(implementation = Expression.class), mediaType = "application/json")
    )
    @PostMapping("/table")
    public ResponseEntity<EmptyResult> table(
            @RequestBody(required = false) @Nullable final Expression exp,
            @RequestHeader(defaultValue = "DEFAULT") final Sort sort,
            @RequestHeader(defaultValue = "NONE") final Hide hide,
            @RequestHeader(defaultValue = "false") final boolean hideIntermediate,
            @RequestHeader(required = false) @Nullable final String lang,
            @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "nb") final String header) {

        log.info("Table call with the following parametres: exp={}, sort={}, hide={}, hideIntermediate={}, lang={}",
                exp, sort, hide, hideIntermediate, lang);

        Language language = Language.setLanguage(lang, header);

        if (exp == null) {
            String message = language == Language.ENGLISH ? "Expression not found in body" : "Uttrykk ikke funnet i body";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }

        try {
            ExpressionUtils.isValid(exp.toString(), language);
        }
        catch (ExpressionInvalidException | MissingCharacterException | TooBigExpressionException e) {
            log.debug(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        final TruthTable table = new TruthTable(exp.toSetArray(hideIntermediate));
        log.debug("New table created: {}", table);

        final ResultOnlyTable tableResult = new ResultOnlyTable(exp.toString(), StringUtils.mapToStrings(table), table);
        final ResponseEntity<EmptyResult> result = ResponseEntity.ok(tableResult);
        log.debug("Result sent: {}", result);

        return result;
    }

    /**
     * @return A simplified expression and a matrix representation of a table with truth values
     * @throws ResponseStatusException if the expression is not valid
     */
    @NotNull
    @Operation(
            summary = "Simplify a truth expression and generate a truth table",
            description = "Simplify a truth expression, and return the result." +
                    " If the expression is not valid, the result will be empty with an error message.",
            tags = {"Simplify", "table"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The expression was valid and a table was generated",
                    content = {@Content(schema = @Schema(implementation = ResultWithTable.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "The expression was not valid",
                    content = {@Content(schema = @Schema(implementation = EmptyResult.class), mediaType = "application/json")}),
    })
    @Parameters(value = {
            @Parameter(name = "exp", description = "The expression to simplify and generate a table for"),
            @Parameter(name = "lang", description = "Overrides the language in the header"),
            @Parameter(name = "simplify", description = "Simplify the expression"),
            @Parameter(name = "sort", description = "Sort the variables in the table"),
            @Parameter(name = "hide", description = "Hide the variables in the table"),
            @Parameter(name = "hideIntermediate", description = "Hide the intermediate steps in the table"),
    })
    @GetMapping("/simplify/table/{exp}")
    public ResponseEntity<EmptyResult> simplifyAndTable(
            @PathVariable @NotNull final String exp,
            @RequestParam(required = false) @Nullable final String lang,
            @RequestParam(defaultValue = "true") final boolean simplify,
            @RequestParam(defaultValue = "false") final boolean caseSensitive,
            @RequestParam(defaultValue = "DEFAULT") final Sort sort,
            @RequestParam(defaultValue = "NONE") final Hide hide,
            @RequestParam(defaultValue = "false") final boolean hideIntermediate,
            @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "nb") @NotNull final String header) {

        log.info("Simplify and table call with the following parametres: exp=" + exp + ", lang=" + lang +
                ", simplify=" + simplify + ", sort=" + sort + ", hide=" + hide + ", hideIntermediate=" +
                hideIntermediate + ", caseSensitive=" + caseSensitive);

        StopWatch sw = new StopWatch();
        final ExpressionUtils eu = getExpressionUtils(exp, lang, header, simplify, caseSensitive, sw);

        final ResponseEntity<EmptyResult> result = simplify(eu, expression -> {

            TruthTable table = new TruthTable(expression.toSetArray(hideIntermediate), hide, sort);
            log.debug("New table created: {}", table);

            return new ResultWithTable(exp, expression.toString(), eu.getOperations(),
                    expression, StringUtils.mapToStrings(table), table);
        });

        if (log.isDebugEnabled()) {
            sw.stop();
            log.info("Expression simplified in: " + sw.getTotalTimeMillis() + "ms");
        }

        log.debug("Result sent: {}", result);
        return result;
    }

    @Operation(
            summary = "Check if an expression is valid",
            description = "Check if an expression is valid, otherwise return an error message.",
            tags = {"Check"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The expression was valid",
                    content = {@Content(schema = @Schema(implementation = EmptyResult.class), mediaType = "text/plain")}),
            @ApiResponse(responseCode = "400", description = "The expression was not valid",
                    content = {@Content(schema = @Schema(implementation = EmptyResult.class), mediaType = "text/plain")}),
    })
    @Parameters(value = {
            @Parameter(name = "exp", description = "The expression to check"),
            @Parameter(name = "lang", description = "Overrides the language in the header"),
    })
    @GetMapping("isLegal/{exp}")
    public ResponseEntity<String> isLegal(
            @PathVariable String exp,
            @RequestParam(required = false) String lang,
            @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "nb") String header) {
        log.info("isLegal call with the following parametres: exp={}, lang={}, header={}", exp, lang, header);

        Language language = Language.setLanguage(lang, header);
        try {
            ExpressionUtils.isValid(exp, language);
        }
        catch (Exception e) {
            log.debug(Arrays.toString(e.getStackTrace()));
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        log.debug("Expression is legal");
        return ResponseEntity.ok("OK");
    }


    @NotNull
    private ExpressionUtils getExpressionUtils(String exp, String lang, String header, boolean simplify, boolean caseSensitive, StopWatch sw) {
        Language language = Language.setLanguage(lang, header);

        final String newExpression = StringUtils.formatString(exp, caseSensitive);
        final ExpressionUtils eu = new ExpressionUtils(newExpression, simplify, language, caseSensitive);

        if (log.isDebugEnabled()) {
            sw.start();
        }
        return eu;
    }

    @NotNull
    private ResponseEntity<EmptyResult> simplify(@NotNull ExpressionUtils eu, @NotNull Function<Expression, EmptyResult> function) {

        final Expression expression;

        try {
            expression = eu.simplify();
        }
        catch (ExpressionInvalidException | MissingCharacterException | TooBigExpressionException e) {
            log.debug(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        log.debug("Expression simplified to: {}", expression);

        return ResponseEntity.ok(function.apply(expression));
    }

}
