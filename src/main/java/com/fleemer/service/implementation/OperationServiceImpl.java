package com.fleemer.service.implementation;

import com.fleemer.model.Operation;
import com.fleemer.model.Person;
import com.fleemer.repository.OperationRepository;
import com.fleemer.service.OperationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OperationServiceImpl extends AbstractService<Operation, Long, OperationRepository>
        implements OperationService {
    private final OperationRepository repository;

    @Autowired
    public OperationServiceImpl(OperationRepository repository) {
        this.repository = repository;
    }

    @Override
    protected OperationRepository getRepository() {
        return repository;
    }

    @Override
    public List<Operation> findAll(Person person) {
        return repository.findAllByInAccountPersonOrOutAccountPersonOrCategoryPerson(person, person, person);
    }

    @Override
    public Page<Operation> findAll(Person person, Pageable pageable) {
        return repository.findAllByInAccountPersonOrOutAccountPersonOrCategoryPerson(person, person, person,
                pageable);
    }
}
