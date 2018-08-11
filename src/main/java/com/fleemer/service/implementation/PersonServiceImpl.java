package com.fleemer.service.implementation;

import com.fleemer.model.Person;
import com.fleemer.repository.PersonRepository;
import com.fleemer.service.PersonService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Optional<Person> findByEmail(String email) {
        return repository.findByEmail(email);
    }
}
