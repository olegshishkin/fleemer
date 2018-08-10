package com.fleemer.model;

import com.fleemer.model.enums.AccountType;
import com.fleemer.model.enums.Currency;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class Account implements Serializable {
    private Long id;
    private String name;
    private AccountType type;
    private Currency currency;
    private BigDecimal balance;
    private Person person;
}
