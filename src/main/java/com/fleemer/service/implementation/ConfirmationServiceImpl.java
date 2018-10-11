package com.fleemer.service.implementation;

import com.fleemer.model.Confirmation;
import com.fleemer.model.Person;
import com.fleemer.repository.ConfirmationRepository;
import com.fleemer.service.ConfirmationService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfirmationServiceImpl extends AbstractService<Confirmation, Long, ConfirmationRepository>
        implements ConfirmationService {
    private final ConfirmationRepository repository;

    @Autowired
    public ConfirmationServiceImpl(ConfirmationRepository repository) {
        this.repository = repository;
    }

    @Override
    protected ConfirmationRepository getRepository() {
        return repository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPersonEnabled(Person person) {
        return repository.findByPersonAndEnabledIsTrue(person).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Confirmation> findByPersonEmail(String email) {
        return repository.findByPersonEmail(email);
    }
}
