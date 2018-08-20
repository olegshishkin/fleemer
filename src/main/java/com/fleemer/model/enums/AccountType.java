package com.fleemer.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AccountType {
    @JsonProperty("Cash")
    CASH,

    @JsonProperty("Bank account")
    BANK_ACCOUNT,

    @JsonProperty("Deposit")
    DEPOSIT,

    @JsonProperty("Debt")
    DEBT
}
