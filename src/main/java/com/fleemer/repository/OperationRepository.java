package com.fleemer.repository;

import com.fleemer.model.Account;
import com.fleemer.model.Category;
import com.fleemer.model.Operation;
import com.fleemer.model.Person;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationRepository extends JpaRepository<Operation, Long> {
    List<Operation> findAllByInAccountPersonOrOutAccountPerson(Person inPerson, Person outPerson);

    Page<Operation> findAllByInAccountPersonOrOutAccountPerson(Person inPerson, Person outPerson, Pageable pageable);

    @Query("SELECT o FROM Operation o LEFT OUTER JOIN o.inAccount a1 LEFT OUTER JOIN o.outAccount a2 " +
            "WHERE o.id = :id AND (a1.person = :inPerson OR a2.person = :outPerson)")
    Optional<Operation> getByIdAndInAccountPersonOrOutAccountPerson(@Param("id") Long id,
                                                                    @Param("inPerson") Person inAccountPerson,
                                                                    @Param("outPerson") Person outAccountPerson);

    long countOperationsByCategory(Category category);

    long countOperationsByInAccountOrOutAccount(Account inAccount, Account outAccount);

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
