package com.fleemer.repository;

import com.fleemer.model.Account;
import com.fleemer.model.Category;
import com.fleemer.model.Operation;
import com.fleemer.model.Person;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationRepository extends JpaRepository<Operation, Long> {
    List<Operation> findAllByInAccountPersonOrOutAccountPersonOrCategoryPerson(Person inPerson, Person outPerson,
                                                                               Person categoryPerson);

    Page<Operation> findAllByInAccountPersonOrOutAccountPersonOrCategoryPerson(Person inPerson, Person outPerson,
                                                                               Person categoryPerson, Pageable pageable);

    List<Operation> findAllByCategory(Category category);

    List<Operation> findAllByInAccountOrOutAccount(Account inAccount, Account outAccount);

    @Query(value =  "SELECT o.date, inSum, outSum\n" +
                    "FROM operation o\n" +
                    "       LEFT JOIN\n" +
                    "         (SELECT o.date date, SUM(o.sum) inSum\n" +
                    "          FROM operation o LEFT JOIN category c on o.category_id = c.id\n" +
                    "          WHERE c.person_id = :person AND c.type = 0 AND o.date BETWEEN :fromDate AND :tillDate\n" +
                    "          GROUP BY o.date) income\n" +
                    "         ON o.date = income.date\n" +
                    "       LEFT JOIN\n" +
                    "         (SELECT o.date date, SUM(o.sum) outSum\n" +
                    "          FROM operation o LEFT JOIN category c on o.category_id = c.id\n" +
                    "          WHERE c.person_id = :person AND c.type = 1 AND o.date BETWEEN :fromDate AND :tillDate\n" +
                    "          GROUP BY o.date) outcome\n" +
                    "         ON o.date = outcome.date\n" +
                    "WHERE (inSum IS NOT NULL OR outSum IS NOT NULL)\n" +
                    "GROUP BY o.date\n" +
                    "ORDER BY o.date", nativeQuery = true)
    List<Object[]> findAllDailyVolumes(@Param("fromDate") LocalDate fromDate, @Param("tillDate") LocalDate tillDate,
                                     @Param("person") Person person);
}
