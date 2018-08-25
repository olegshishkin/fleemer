package com.fleemer.service;

import com.fleemer.model.Account;
import com.fleemer.model.Person;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountService extends BaseService<Account, Long> {
    Optional<Account> findByNameAndPerson(String name, Person person);

    List<Account> findAll(Person person);

    BigDecimal getTotalBalance(Person person);

    Optional<Account> findByIdAndPerson(Long id, Person person);
}
