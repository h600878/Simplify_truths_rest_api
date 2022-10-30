package com.github.martials.controllers;

import com.github.martials.Language;
import com.github.martials.Result;
import com.github.martials.SimplifyTruthsRestApiApplication;
import com.github.martials.Status;
import com.github.martials.expressions.Expression;
import com.github.martials.utils.ExpressionUtils;
import com.github.martials.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpHeaders;
import java.util.Objects;

@RestController
public class ApiController {

    /**
     * @param exp A truth expression
     * @param lang Overrides the language in the header
     * @param header The accept language section of the header, the prefered language will be used, unless english is set
     * @return The result of the simplified expression, or null if not valid
     */
    @NotNull
    @GetMapping("/api")
    public Result simplify(@RequestParam(required = false) @NotNull final String exp,
                           @RequestParam(required = false) final String lang,
                           @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "nb") String header) {

        System.err.println("Language=" + header);

        setLanguage(lang, header);

        final Expression expression;

        System.err.println("Original=" + exp);
        String newExpression = exp.replace(" ", "");

        newExpression = StringUtils.replaceOperators(newExpression);
        System.err.println("Converted=" + newExpression);

        final String isLegal = ExpressionUtils.isLegalExpression(newExpression);

        if (Objects.equals(isLegal, "")) {
            expression = ExpressionUtils.simplify(newExpression, true);
        }
        else {
            expression = null;
        }

        return new Result(expression != null ? Status.OK : new Status(500, isLegal),
                exp,
                expression != null ? expression.toString() : newExpression,
                Expression.getOrderOfOperations(),
                expression);
    }

    private void setLanguage(String language, @NotNull String header) {
        if (language != null) {
            boolean isFound = false;
            for (var lang : Language.values()) {
                if (language.equalsIgnoreCase(lang.getLang())) {
                    SimplifyTruthsRestApiApplication.lang = lang;
                    isFound = true;
                }
            }
            if (!isFound) {
                System.err.println("Language was not found");
            }
        }
        else if (header.substring(0, 2).equalsIgnoreCase("en")){
            SimplifyTruthsRestApiApplication.lang = Language.english;
        }
    }

}
