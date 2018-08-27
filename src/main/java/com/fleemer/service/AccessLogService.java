package com.fleemer.service;

import com.fleemer.model.Person;
import com.fleemer.model.mongodb.AccessStats;
import java.time.LocalDate;
import java.util.List;

public interface AccessLogService {
    List<AccessStats> findAll(Person person, LocalDate from, LocalDate till);
}
