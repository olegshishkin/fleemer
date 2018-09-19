package com.fleemer.service.implementation;

import com.fleemer.model.Person;
import com.fleemer.repository.PersonRepository;
import com.fleemer.service.PersonService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PersonServiceImpl extends AbstractService<Person, Long, PersonRepository> implements PersonService {
    private final PersonRepository repository;

    @Autowired
    public PersonServiceImpl(PersonRepository repository) {
        this.repository = repository;
    }

    @Override
    protected PersonRepository getRepository() {
        return repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Person> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    public Page<Person> findAllByNicknamePart(String text, Pageable pageable) {
        return repository.findAllByNicknameContainsIgnoreCase(text, pageable);
    }

    @Override
    public Optional<Person> findByNickname(String nickname) {
        return repository.findByNickname(nickname);
    }
}
