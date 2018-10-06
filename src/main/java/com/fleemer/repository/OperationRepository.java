package com.fleemer.repository;

import com.fleemer.model.Account;
import com.fleemer.model.Category;
import com.fleemer.model.Operation;
import com.fleemer.model.Person;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationRepository extends JpaRepository<Operation, Long>, JpaSpecificationExecutor<Operation> {
    long countOperationsByCategory(Category category);

    long countOperationsByInAccountOrOutAccount(Account inAccount, Account outAccount);

    List<Operation> findAllByInAccountPersonOrOutAccountPerson(Person inPerson, Person outPerson);

    @Query("SELECT o FROM Operation o LEFT OUTER JOIN o.inAccount a1 LEFT OUTER JOIN o.outAccount a2 " +
            "WHERE o.date BETWEEN :fromDate AND :tillDate AND (a1.person = :inPerson OR a2.person = :outPerson)")
    List<Operation> findAllByInAccountPersonOrOutAccountPersonAndDateBetween(@Param("inPerson") Person inPerson,
                                                                             @Param("outPerson") Person outPerson,
                                                                             @Param("fromDate") LocalDate fromDate,
                                                                             @Param("tillDate") LocalDate tillDate);

    @Query("SELECT o FROM Operation o LEFT OUTER JOIN o.inAccount a1 LEFT OUTER JOIN o.outAccount a2 " +
            "WHERE o.id = :id AND (a1.person = :inPerson OR a2.person = :outPerson)")
    Optional<Operation> getByIdAndInAccountPersonOrOutAccountPerson(@Param("id") Long id,
                                                                    @Param("inPerson") Person inAccountPerson,
                                                                    @Param("outPerson") Person outAccountPerson);

    @Query(value =  "SELECT o.date, inSum, outSum " +
                    "FROM operation o " +
                        "LEFT JOIN " +
                            "(SELECT o.date date, SUM(o.sum) inSum " +
                                "FROM operation o " +
                                    "LEFT JOIN category c on o.category_id = c.id " +
                                    "LEFT JOIN account a ON o.in_account_id = a.id " +
                                "WHERE c.person_id = :person " +
                                    "AND c.type = 0 " +
                                    "AND a.currency = :currency " +
                                    "AND o.date BETWEEN :fromDate AND :tillDate " +
                                "GROUP BY o.date) income " +
                            "ON o.date = income.date " +
                        "LEFT JOIN " +
                            "(SELECT o.date date, SUM(o.sum) outSum " +
                                "FROM operation o " +
                                    "LEFT JOIN category c on o.category_id = c.id " +
                                    "LEFT JOIN account a ON o.out_account_id = a.id " +
                                "WHERE c.person_id = :person " +
                                    "AND c.type = 1 " +
                                    "AND a.currency = :currency " +
                                    "AND o.date BETWEEN :fromDate AND :tillDate " +
                                "GROUP BY o.date) outcome " +
                            "ON o.date = outcome.date " +
                    "WHERE (inSum IS NOT NULL OR outSum IS NOT NULL) " +
                    "GROUP BY o.date " +
                    "ORDER BY o.date ", nativeQuery = true)
    List<Object[]> findAllDailyVolumes(@Param("currency") int currencyOrdinal, @Param("fromDate") LocalDate fromDate,
                                       @Param("tillDate") LocalDate tillDate, @Param("person") Person person);
}
