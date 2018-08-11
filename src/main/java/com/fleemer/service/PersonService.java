package com.fleemer.service;

import com.fleemer.model.Person;
import java.util.Optional;

public interface PersonService extends BaseService<Person, Long> {
    Optional<Person> findByEmail(String email);
}
