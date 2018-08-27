package com.fleemer.service.implementation;

import com.fleemer.model.Person;
import com.fleemer.model.mongodb.AccessStats;
import com.fleemer.repository.AccessLogRepository;
import com.fleemer.service.AccessLogService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccessLogServiceImpl implements AccessLogService {
    private final AccessLogRepository repository;

    @Autowired
    public AccessLogServiceImpl(AccessLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<AccessStats> findAll(Person person, LocalDate from, LocalDate till) {
        return repository.findAll(person, from, till);
    }
}
