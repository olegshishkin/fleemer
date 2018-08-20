package com.fleemer.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum CategoryType {
    @JsonProperty("Income")
    INCOME,

    @JsonProperty("Outcome")
    OUTCOME
}
