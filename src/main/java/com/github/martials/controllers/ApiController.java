package com.github.martials.controllers;

import com.github.martials.Result;
import com.github.martials.classes.Expression;
import com.github.martials.classes.ExpressionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class ApiController {

    /**
     * @param exp A truth expression
     * @return The result of the simplified expression, or null if not valid
     */
    @NotNull
    @GetMapping("/api")
    public Result simplify(@RequestParam(required = false) @NotNull final String exp) {

        final Expression expression;

        System.err.println("Original=" + exp);
        String newExpression = exp.replace(" ", "");

        newExpression = ExpressionUtils.replaceOperators(newExpression);
        System.err.println("Converted=" + newExpression);

        final String isLegal = ExpressionUtils.isLegalExpression(newExpression);

        if (Objects.equals(isLegal, "")) {
            expression = ExpressionUtils.simplify(newExpression, true);
        }
        else {
            expression = null;
        }

        return new Result("200", exp, expression != null ? expression.toString() : newExpression, expression);
    }

}
