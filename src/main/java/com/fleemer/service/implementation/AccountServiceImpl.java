package com.fleemer.service.implementation;

import com.fleemer.model.Account;
import com.fleemer.repository.AccountRepository;
import com.fleemer.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
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
}
