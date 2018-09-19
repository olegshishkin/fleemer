package com.fleemer.security;

import com.fleemer.model.Person;
import com.fleemer.service.ConfirmationService;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class PersonDetails implements UserDetails {
    private Person person;
    private final ConfirmationService confirmationService;

    public PersonDetails(Person person, ConfirmationService confirmationService) {
        this.person = person;
        this.confirmationService = confirmationService;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("USER");
        return List.of(grantedAuthority);
    }

    @Override
    public String getPassword() {
        return person.getHash();
    }

    @Override
    public String getUsername() {
        return person.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return confirmationService.isPersonEnabled(person);
    }
}
