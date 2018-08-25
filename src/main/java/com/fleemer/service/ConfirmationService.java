package com.fleemer.service;

import com.fleemer.model.Confirmation;
import com.fleemer.model.Person;
import java.util.Optional;

public interface ConfirmationService extends BaseService<Confirmation, Long> {
    boolean isPersonEnabled(Person person);

    Optional<Confirmation> findByPersonEmail(String email);
}
