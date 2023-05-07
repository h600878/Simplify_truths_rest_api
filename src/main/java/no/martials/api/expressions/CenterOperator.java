package no.martials.api.expressions;

import no.martials.api.enums.Operator;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CenterOperator", description = "A record containing an operator and its index in the expression")
public record CenterOperator(Operator operator, int index) {
}
