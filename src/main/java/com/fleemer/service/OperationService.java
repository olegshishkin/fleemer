package com.fleemer.service;

import com.fleemer.model.Account;
import com.fleemer.model.Category;
import com.fleemer.model.Operation;
import com.fleemer.model.Person;
import com.fleemer.service.exception.ServiceException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

public interface OperationService extends BaseService<Operation, Long> {
    List<Operation> findAllByPerson(Person person, @Nullable LocalDate from, @Nullable LocalDate till)
            throws ServiceException;

    Page<Operation> findAllByPerson(Person person, @Nullable LocalDate from, @Nullable LocalDate till,
                                    Pageable pageable) throws ServiceException;

    Optional<Operation> findByIdAndPerson(Long id, Person person);

    long countOperationsByCategory(Category category);

    long countOperationsByAccount(Account account);

    List<Object[]> findAllDailyVolumes(LocalDate fromDate, LocalDate tillDate, Person person) throws ServiceException;
}
