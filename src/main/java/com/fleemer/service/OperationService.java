package com.fleemer.service;

import com.fleemer.model.Account;
import com.fleemer.model.Category;
import com.fleemer.model.Operation;
import com.fleemer.model.Person;
import com.fleemer.model.enums.Currency;
import com.fleemer.service.exception.ServiceException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

public interface OperationService extends BaseService<Operation, Long> {
    long countOperationsByCategory(Category category);

    long countOperationsByAccount(Account account);

    List<Operation> findAll(Person person);

    List<Operation> findAll(Person person, LocalDate from, LocalDate till);

    Page<Operation> findAll(Person person, Pageable pageable, boolean orMode, @Nullable LocalDate from,
                            @Nullable LocalDate till, @Nullable List<Account> inAccounts,
                            @Nullable List<Account> outAccounts, @Nullable List<Category> categories,
                            @Nullable BigDecimal min, @Nullable BigDecimal max, @Nullable String comment)
            throws ServiceException;

    Optional<Operation> findByIdAndPerson(Long id, Person person);

    List<Object[]> findAllDailyVolumes(Person person, Currency currency, LocalDate fromDate, LocalDate tillDate)
            throws ServiceException;
}
