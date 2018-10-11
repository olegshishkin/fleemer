package com.fleemer.web.form.validator;

import com.fleemer.model.Person;
import com.fleemer.service.PersonService;
import com.fleemer.web.form.PersonForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class PersonFormValidator implements Validator {
    private static final String PASSWD_CONFIRM_FAILED_MSG_KEY = "user.error.password-not-equals";
    private static final String USER_EXISTS_ERROR_MSG_KEY = "user.error.user-exists";

    private final PersonService personService;
    private final MessageSource messageSource;

    @Autowired
    public PersonFormValidator(PersonService personService, MessageSource messageSource) {
        this.personService = personService;
        this.messageSource = messageSource;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return PersonForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PersonForm personForm = (PersonForm) target;
        Person person = personForm.getPerson();
        if (!personForm.getConfirmPassword().equals(person.getHash())) {
            String field = "person.hash";
            errors.rejectValue(field, field + ".confirmNotEquals", getMessage(PASSWD_CONFIRM_FAILED_MSG_KEY));
        }
        personService.findByEmail(person.getEmail()).ifPresent(p -> {
            String field = "person.email";
            isAnotherExists(errors, person, p, field, field + ".alreadyExists");
        });
        personService.findByNickname(person.getNickname()).ifPresent(p -> {
            String field = "person.nickname";
            isAnotherExists(errors, person, p, field, field + ".alreadyExists");
        });
    }

    private void isAnotherExists(Errors errors, Person formPerson, Person persistedPerson, String field,
                                 String errorCode) {
        if (!persistedPerson.getId().equals(formPerson.getId())) {
            errors.rejectValue(field, errorCode, getMessage(USER_EXISTS_ERROR_MSG_KEY));
        }
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
