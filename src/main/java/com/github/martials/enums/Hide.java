package com.github.martials.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Hide",
        description = "Enum to hide the rows in the table. " +
                "NONE: no rows are hidden, TRUE: only the rows with true are hidden, " +
                "FALSE: only the rows with false are hidden")
public enum Hide {
    NONE,
    TRUE,
    FALSE
}
