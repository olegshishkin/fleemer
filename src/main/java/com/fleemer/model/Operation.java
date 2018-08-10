package com.fleemer.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class Operation implements Serializable {
    private Long id;
    private LocalDate date;
    private Account inAccount;
    private Account outAccount;
    private Category category;
    private BigDecimal sum;
    private String comment;
}
