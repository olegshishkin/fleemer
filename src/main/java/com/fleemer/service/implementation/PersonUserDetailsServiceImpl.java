package com.fleemer.service.implementation;

import com.fleemer.model.Person;
import com.fleemer.security.PersonDetails;
import com.fleemer.service.PersonUserDetailsService;
import com.fleemer.service.PersonService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PersonUserDetailsServiceImpl implements PersonUserDetailsService {
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
        throw new UsernameNotFoundException("No person with such email.");
    }
}
