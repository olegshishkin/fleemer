package com.fleemer.repository;

import com.fleemer.model.Account;
import com.fleemer.model.Category;
import com.fleemer.model.Operation;
import com.fleemer.model.Person;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationRepository extends JpaRepository<Operation, Long> {
    List<Operation> findAllByInAccountPersonOrOutAccountPersonOrCategoryPerson(Person inPerson, Person outPerson,
                                                                               Person categoryPerson);

    Page<Operation> findAllByInAccountPersonOrOutAccountPersonOrCategoryPerson(Person inPerson, Person outPerson,
                                                                               Person categoryPerson, Pageable pageable);

    List<Operation> findAllByCategory(Category category);

    List<Operation> findAllByInAccountOrOutAccount(Account inAccount, Account outAccount);
}
