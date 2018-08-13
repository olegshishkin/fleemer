package com.fleemer.repository;

import com.fleemer.model.Account;
import com.fleemer.model.Person;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findAllByPersonOrderByName(Person person);

    Optional<Account> findByNameAndPerson(String name, Person person);

    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.person = :person")
    BigDecimal getTotalBalance(@Param("person") Person person);

    Optional<Account> getByIdAndPerson(Long id, Person person);
}
