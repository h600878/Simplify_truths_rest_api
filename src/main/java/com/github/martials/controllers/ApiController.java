package com.github.martials.controllers;

import com.github.martials.enums.Language;
import com.github.martials.Result;
import com.github.martials.SimplifyTruthsRestApiApplication;
import com.github.martials.Status;
import com.github.martials.expressions.Expression;
import com.github.martials.utils.ExpressionUtils;
import com.github.martials.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Objects;

@RestController
public class ApiController {

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
    @CrossOrigin(origins = "http://localhost:8000")
    public Result simplify(@RequestParam(defaultValue = "") @NotNull final String exp,
                           @RequestParam(required = false) @Nullable final String lang,
                           @RequestParam(defaultValue = "true") final boolean simplify,
                           @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "nb") String header) {

        log.info("API call with the following parametres: exp=" + exp + ", lang=" + lang + ", simplify=" + simplify);
        log.info("ACCEPT_LANGUAGE header=" + header);

        setLanguage(lang, header);
        log.info("Language set to " + SimplifyTruthsRestApiApplication.lang);

        if (exp.equals("")) {
            log.warn("Expression is empty, exiting...");
            return new Result(Status.NOT_FOUND, "", "", null, null);
        }

        final Expression expression;

        String newExpression = exp.replace(" ", "");
        log.debug("Whitespace removed in expression: {}", newExpression);

        newExpression = StringUtils.replaceOperators(newExpression);
        log.debug("Expression changed to: {}", newExpression);

        final String isLegal = ExpressionUtils.isLegalExpression(newExpression);

        if (Objects.equals(isLegal, "")) {
            expression = ExpressionUtils.simplify(newExpression, simplify);
            log.debug("Expression simplified to: {}", expression);
        }
        else {
            log.error("Expression is not legal: {}", isLegal);
            expression = null;
        }

        final Result result = new Result(expression != null ? Status.OK : new Status(500, isLegal),
                exp,
                expression != null ? expression.toString() : newExpression,
                Expression.getOrderOfOperations(),
                expression);

        log.debug("Result sent: {}", result);
        return result;
    }

    /**
     *
     * @return A matrix representation of a table with truth values
     */
    @NotNull
    @GetMapping("/table")
    @CrossOrigin(origins = "http://localhost:8000")
    public Result table() {
        return null;
    }

    /**
     *
     * @return A simplified expression and a matrix representation of a table with truth values
     */
    @NotNull
    @GetMapping("/simplify/table")
    @CrossOrigin(origins = "http://localhost:8000")
    public Result simplifyAndTable() {
        return null;
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
                System.err.println("Language was not found");
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

}
