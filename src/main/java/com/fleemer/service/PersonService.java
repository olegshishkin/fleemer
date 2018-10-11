package com.fleemer.service;

import com.fleemer.model.Person;
import com.fleemer.service.exception.ServiceException;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PersonService extends BaseService<Person, Long> {
    Optional<Person> findByEmail(String email);

    Page<Person> findAllByNicknamePart(String text, Pageable pageable);

    Optional<Person> findByNickname(String nickname);

    void saveAndConfirm(Person person, String token) throws ServiceException;
}
