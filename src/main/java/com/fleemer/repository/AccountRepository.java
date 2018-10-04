package com.fleemer.repository;

import com.fleemer.model.Account;
import com.fleemer.model.Person;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findAllByPersonOrderByName(Person person);

    Optional<Account> findByNameAndPerson(String name, Person person);

    Optional<Account> findByIdAndPerson(Long id, Person person);
}
