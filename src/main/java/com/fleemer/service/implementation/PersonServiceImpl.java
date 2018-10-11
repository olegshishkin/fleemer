package com.fleemer.service.implementation;

import com.fleemer.model.Confirmation;
import com.fleemer.model.Person;
import com.fleemer.repository.PersonRepository;
import com.fleemer.service.ConfirmationService;
import com.fleemer.service.PersonService;
import com.fleemer.service.exception.ServiceException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersonServiceImpl extends AbstractService<Person, Long, PersonRepository> implements PersonService {
    private final PersonRepository repository;
    private final ConfirmationService confirmationService;

    @Autowired
    public PersonServiceImpl(PersonRepository repository, ConfirmationService confirmationService) {
        this.repository = repository;
        this.confirmationService = confirmationService;
    }

    @Override
    protected PersonRepository getRepository() {
        return repository;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Optional<Person> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Page<Person> findAllByNicknamePart(String text, Pageable pageable) {
        return repository.findAllByNicknameContainsIgnoreCase(text, pageable);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Optional<Person> findByNickname(String nickname) {
        return repository.findByNickname(nickname);
    }

    @Override
    @Transactional
    public void saveAndConfirm(Person person, String token) throws ServiceException {
        Confirmation confirmation = new Confirmation();
        confirmation.setPerson(person);
        confirmation.setToken(token);
        super.save(person);//todo
        confirmationService.save(confirmation);
    }
}
