package com.fleemer.repository;

import com.fleemer.model.Confirmation;
import com.fleemer.model.Person;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfirmationRepository extends JpaRepository<Confirmation, Long> {
    Optional<Confirmation> findByPersonAndEnabledIsTrue(Person person);

    Optional<Confirmation> findByPersonEmail(String email);
}
