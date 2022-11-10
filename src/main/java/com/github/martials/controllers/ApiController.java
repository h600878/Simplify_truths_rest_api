package com.github.martials.controllers;

import com.github.martials.results.Result;
import com.github.martials.results.ResultOnlyTable;
import com.github.martials.results.ResultWithTable;
import com.github.martials.SimplifyTruthsRestApiApplication;
import com.github.martials.Status;
import com.github.martials.enums.Hide;
import com.github.martials.enums.Language;
import com.github.martials.enums.Sort;
import com.github.martials.expressions.Expression;
import com.github.martials.expressions.TruthTable;
import com.github.martials.utils.ExpressionUtils;
import com.github.martials.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
public class ApiController { // TODO test! table getMapping give different results

    private final Logger log = LoggerFactory.getLogger(ApiController.class);

    /**
     * @param exp      A truth expression
     * @param lang     Overrides the language in the header
     * @param header   The accept language section of the header, the prefered language will be used, unless english is set
     * @param simplify Wheter or not to simplify the given expression
     * @return The result of the simplified expression, or null if not valid
     */
    @NotNull
    @GetMapping("/simplify")
    @CrossOrigin(origins = {"http://localhost:8000", "https://h600878.github.io/"})
    public Result simplify(@RequestParam(required = false) @Nullable final String exp,
                           @RequestParam(required = false) @Nullable final String lang,
                           @RequestParam(defaultValue = "true") final boolean simplify,
                           @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "nb") final String header) {

        log.info("Simplify call with the following parametres: exp=" + exp + ", lang=" + lang + ", simplify=" + simplify);

        setAndLogLanguage(lang, header);

        if (exp == null) {
            log.warn("Parametre exp is empty, exiting...");
            return new Result(Status.NOT_FOUND, "", "", null, null);
        }

        final String newExpression = replace(exp);
        final String isLegal = ExpressionUtils.isLegalExpression(newExpression);

        final Expression expression = simplifyIfLegal(simplify, newExpression, isLegal);

        final Result result = new Result(expression != null ? Status.OK : new Status(500, isLegal),
                exp,
                expression != null ? expression.toString() : newExpression,
                Expression.getOrderOfOperations(),
                expression);

        log.debug("Result sent: {}", result);
        return result;
    }

    /**
     * @return A matrix representation of a table with truth values
     */
    @NotNull
    @GetMapping("/table")
    @CrossOrigin(origins = {"http://localhost:8000", "https://h600878.github.io/"})
    public ResultOnlyTable table(
            @RequestBody(required = false) @Nullable final Expression exp,
            @RequestParam(defaultValue = "defaultSort") final Sort sort,
            @RequestParam(defaultValue = "none") final Hide hide,
            @RequestParam(required = false) @Nullable final String lang,
            @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "nb") final String header) {

        log.info("GetMapping with the following parametres: exp={}, sort={}, hide={}, lang={}", exp, sort, hide, lang);

        setAndLogLanguage(lang, header);

        if (exp == null) {
            log.warn("Body is empty, exiting...");
            return new ResultOnlyTable(Status.NOT_FOUND, "", null);
        }

        final TruthTable table = new TruthTable(exp.toSetArray());
        log.debug("New table created: {}", table);

        final ResultOnlyTable result = new ResultOnlyTable(Status.OK, exp.toString(), table);
        log.debug("Result sent: {}", result);

        return result;
    }

    /**
     * @return A simplified expression and a matrix representation of a table with truth values
     */
    @NotNull
    @GetMapping("/simplify/table")
    @CrossOrigin(origins = {"http://localhost:8000", "https://h600878.github.io/"})
    public ResultWithTable simplifyAndTable(
            @RequestParam(required = false) @Nullable final String exp,
            @RequestParam(required = false) @Nullable final String lang,
            @RequestParam(defaultValue = "true") final boolean simplify,
            @RequestParam(defaultValue = "defaultSort") final Sort sort,
            @RequestParam(defaultValue = "none") final Hide hide,
            @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "nb") @NotNull final String header) {

        log.info("Simplify and table call with the following parametres: exp=" + exp + ", lang=" + lang + ", simplify="
                + simplify + ", sort=" + sort + ", hide=" + hide);

        setAndLogLanguage(lang, header);

        if (exp == null) {
            log.warn("Parametre exp is empty, exiting...");
            return new ResultWithTable(Status.NOT_FOUND, "", "", null, null, null);
        }

        final String newExpression = replace(exp);


        final String isLegal = ExpressionUtils.isLegalExpression(newExpression);
        final Expression expression = simplifyIfLegal(simplify, newExpression, isLegal);

        final TruthTable table;
        if (Objects.equals(isLegal, "") && expression != null) {
            table = new TruthTable(expression.toSetArray());
            log.debug("New table created: {}", table);
        }
        else {
            table = null;
        }

        final ResultWithTable result = new ResultWithTable(expression != null ? Status.OK : new Status(500, isLegal),
                exp,
                expression != null ? expression.toString() : newExpression,
                Expression.getOrderOfOperations(),
                expression,
                table);

        log.debug("Result sent: {}", result);
        return result;
    }

    @Nullable
    private Expression simplifyIfLegal(boolean simplify, String newExpression, @NotNull String isLegal) {
        final Expression expression;
        if (isLegal.equals("")) {
            expression = ExpressionUtils.simplify(newExpression, simplify);
            log.debug("Expression simplified to: {}", expression);
        }
        else {
            log.error("Expression is not legal: {}", isLegal);
            expression = null;
        }
        return expression;
    }

    private void setAndLogLanguage(@Nullable String lang, @NotNull String header) {
        log.info("ACCEPT_LANGUAGE header=" + header);
        setLanguage(lang, header);
        log.info("Language set to " + SimplifyTruthsRestApiApplication.lang);
    }

    private void setLanguage(String language, @NotNull String header) {
        final String headerLang = header.substring(0, 2);
        final List<String> norLangs = List.of("nb", "no", "nn");

        if (language != null) {
            boolean isFound = false;
            for (Language lang : Language.values()) {
                if (language.equalsIgnoreCase(lang.getLang())) {
                    SimplifyTruthsRestApiApplication.lang = lang;
                    isFound = true;
                }
            }
            if (!isFound) {
                log.warn("Language was not found");
            }
        }
        else if (headerLang.equalsIgnoreCase("en")) {
            SimplifyTruthsRestApiApplication.lang = Language.english;
        }
        else if (!norLangs.contains(headerLang.toLowerCase())) { // If neither "en", or the Norwegian languages
            try {
                setLanguage(null, header.split(",")[1]);
            }
            catch (IndexOutOfBoundsException ignored) {
                log.warn("No language recognized in ACCEPT_LANGUAGE");
            }
        }
    }

    @NotNull
    private String replace(@NotNull String expression) {
        String newExpression = expression.replace(" ", "");
        log.debug("Whitespace removed in expression: {}", newExpression);

        newExpression = StringUtils.replaceOperators(newExpression);
        log.debug("Expression changed to: {}", newExpression);
        return newExpression;
    }

}
