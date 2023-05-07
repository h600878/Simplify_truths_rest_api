package no.martials.api.expressions;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jetbrains.annotations.NotNull;

@Schema(name = "OrderOperations", description = "A record containing the expression before and after simplifying, and the law used")
public record OrderOperations(@NotNull String before, @NotNull String after, @NotNull String law) {

}
