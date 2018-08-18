package com.fleemer.service.implementation;

import com.fleemer.model.Person;
import com.fleemer.security.PersonDetails;
import com.fleemer.service.PersonUserDetailsService;
import com.fleemer.service.PersonService;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PersonUserDetailsServiceImpl implements PersonUserDetailsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonUserDetailsServiceImpl.class);
    private static final String NO_PERSON_ERROR = "No person with such email.";

    private final PersonService service;

    @Autowired
    public PersonUserDetailsServiceImpl(PersonService service) {
        this.service = service;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        Optional<Person> person = service.findByEmail(s);
        if (person.isPresent()) {
            return new PersonDetails(person.get());
        }
        LOGGER.error("UsernameNotFoundException: {}", NO_PERSON_ERROR);
        throw new UsernameNotFoundException(NO_PERSON_ERROR);
    }
}
