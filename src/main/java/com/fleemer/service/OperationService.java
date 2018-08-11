package com.fleemer.service;

import com.fleemer.model.Operation;
import com.fleemer.model.Person;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OperationService extends BaseService<Operation, Long> {
    List<Operation> findAll(Person person);

    Page<Operation> findAll(Person person, Pageable pageable);
}
