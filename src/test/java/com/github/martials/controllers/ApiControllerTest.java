package com.github.martials.controllers;

import com.github.martials.enums.Hide;
import com.github.martials.enums.Operator;
import com.github.martials.enums.Sort;
import com.github.martials.results.EmptyResult;
import com.github.martials.results.Result;
import com.github.martials.results.ResultOnlyTable;
import com.github.martials.utils.ExpressionUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

public class ApiControllerTest {

    private ApiController ac;

    @BeforeEach
    void setUp() {
        ac = new ApiController();
    }

    @NotNull
    private ResponseEntity<EmptyResult> defaultSimplify(@NotNull String expression) {
        return ac.simplify(expression, null, true, false, "nb");
    }

    @NotNull
    private ResponseEntity<EmptyResult> defaultDontSimplify(@NotNull String expression) {
        return ac.simplify(expression, null, false, false, "nb");
    }

    @ParameterizedTest
    @ValueSource(strings = {"a&b", "a|b", "a->b", "a     &    ¬b |     c"})
    void simplifyLegalExpression(String expression) {
        ResponseEntity<EmptyResult> responseEntity = defaultSimplify(expression);
        assertTrue(responseEntity.hasBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assert responseEntity.getBody() != null;
        assertEquals(Result.class, responseEntity.getBody().getClass());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a&b&", "a::", "a->b->", "a^c", "a%", ""})
    void simplifyIllegalExpression(String expression) {
        try {
            defaultSimplify(expression);
            fail("Should throw exception");
        }
        catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            assertTrue(e.getMessage().contains("Mangler tegn") || e.getMessage().contains("Ulovlig karakter"), e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"A & A", "A -> B", "A | B & B | A"})
    void dontSimplifyLegalExpression(String expression) {
        ResponseEntity<EmptyResult> responseEntity = defaultDontSimplify(expression);
        assertTrue(responseEntity.hasBody());
        Result result = (Result) responseEntity.getBody();

        assert result != null && result.getExpression() != null;

        for (Operator operator : Operator.values()) {
            expression = expression.replaceAll(operator.getInputOperator(), String.valueOf(operator.getOutputOperator()));
        }

        assertEquals(expression, result.getExpression().toString());
    }

    @Test
    void simplifySetLangToEnglish() {
        try {
            ac.simplify("", "en", true, false, "nb");
        }
        catch (ResponseStatusException e) {
            assertTrue(e.getMessage().contains("Missing character"));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"A & a", "A | A"})
    void simplifyCaseInsensitive(String expression) {
        ResponseEntity<EmptyResult> responseEntity = defaultSimplify(expression);

        Result body = (Result) responseEntity.getBody();
        assert body != null && body.getExpression() != null;
        assertEquals(body.getExpression().getLeft(), body.getExpression().getRight());
    }

    @ParameterizedTest
    @ValueSource(strings = {"A&a", "help | Help"})
    void simplifyCaseSensitive(String expression) {
        ResponseEntity<EmptyResult> responseEntity = ac.simplify(expression, null, true, true, "nb");

        Result body = (Result) responseEntity.getBody();
        assert body != null && body.getExpression() != null;
        assertNotNull(body.getExpression().getOperator());
        assertNotEquals(body.getExpression().getLeft(), body.getExpression().getRight());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "^", "(A & B"})
    void simplifyCustomAcceptLanguage(String expression) {
        try {
            ac.simplify(expression, null, true, false, "en");
            fail("Should throw exception");
        }
        catch (ResponseStatusException e) {
            assertTrue(e.getMessage().contains("Missing character") || e.getMessage().contains("Illegal char"));
        }
    }

    @NotNull
    private ResponseEntity<EmptyResult> defaultTable(@NotNull String expression) {
        return ac.table(new ExpressionUtils(expression).simplify(), Sort.DEFAULT, Hide.NONE, false, null, "nb");
    }

    @Test
    void tableNullExpression() {
        try {
            ac.table(null, Sort.DEFAULT, Hide.NONE, false, null, "nb");
        }
        catch (ResponseStatusException e) {
            assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
            assertTrue(e.getMessage().contains("Uttrykk ikke funnet"));
        }
    }

    @Test
    void tableNullExpressionEnLang() {
        try {
            ac.table(null, Sort.DEFAULT, Hide.NONE, false, "en", "nb");
            fail("Should throw exception");
        }
        catch (ResponseStatusException e) {
            assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
            assertTrue(e.getMessage().contains("Expression not found"));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"A ⋀ B", "A ⋁ B", "A ➔ B", "A ⋁ B ⋀ C", "¬A ⋁ B ➔ C"})
    void tableLegalExpression(String expression) {
        ResponseEntity<EmptyResult> responseEntity = defaultTable(expression);
        assertTrue(responseEntity.hasBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assert responseEntity.getBody() != null;
        assertEquals(ResultOnlyTable.class, responseEntity.getBody().getClass());
    }

}
