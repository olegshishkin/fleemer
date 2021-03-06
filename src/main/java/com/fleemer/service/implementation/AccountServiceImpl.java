package com.fleemer.service.implementation;

import com.fleemer.model.Account;
import com.fleemer.model.Person;
import com.fleemer.repository.AccountRepository;
import com.fleemer.service.AccountService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountServiceImpl extends AbstractService<Account, Long, AccountRepository> implements AccountService {
    private final AccountRepository repository;

    @Autowired
    public AccountServiceImpl(AccountRepository repository) {
        this.repository = repository;
    }

    @Override
    protected AccountRepository getRepository() {
        return repository;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Optional<Account> findByNameAndPerson(String name, Person person) {
        return repository.findByNameAndPerson(name, person);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Account> findAll(Person person) {
        return repository.findAllByPersonOrderByName(person);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Optional<Account> findByIdAndPerson(Long id, Person person) {
        return repository.findByIdAndPerson(id, person);
    }
}
