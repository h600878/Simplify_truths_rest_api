package com.github.martials.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Sort", description = "Sort enum. Default is no sort, TRUE_FIRST is true first, FALSE_FIRST is false first")
public enum Sort {
    DEFAULT,
    TRUE_FIRST,
    FALSE_FIRST
}
