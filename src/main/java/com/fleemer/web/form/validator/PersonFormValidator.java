package com.fleemer.web.form.validator;

import com.fleemer.model.Person;
import com.fleemer.service.ConfirmationService;
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
    private static final String CONFIRM_NOT_EQUAL_ERROR_CODE = "person.hash.confirmNotEqual";
    private static final String EMAIL_ALREADY_EXISTS_ERROR_CODE = "person.email.alreadyExists";
    private static final String NICKNAME_ALREADY_EXISTS_ERROR_CODE = "person.nickname.alreadyExists";
    private static final String PASSWD_CONFIRM_FAILED_MSG_KEY = "user.error.password-not-equals";
    private static final String USER_EXISTS_ERROR_MSG_KEY = "user.error.user-exists";

    private final ConfirmationService confirmationService;
    private final PersonService personService;
    private final MessageSource messageSource;

    @Autowired
    public PersonFormValidator(ConfirmationService confirmationService, PersonService personService,
                               MessageSource messageSource) {
        this.confirmationService = confirmationService;
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
            errors.rejectValue("person.hash", CONFIRM_NOT_EQUAL_ERROR_CODE, getMessage(PASSWD_CONFIRM_FAILED_MSG_KEY));
        }
        personService.findByEmail(person.getEmail()).ifPresent(persistentPerson -> {
            boolean isPersonEnabled = confirmationService.isPersonEnabled(persistentPerson);
            if (isPersonEnabled && !persistentPerson.getId().equals(person.getId())) {
                String field = "person.email";
                errors.rejectValue(field, EMAIL_ALREADY_EXISTS_ERROR_CODE, getMessage(USER_EXISTS_ERROR_MSG_KEY));
            }
        });
        personService.findByNickname(person.getNickname()).ifPresent(persistentPerson -> {
            if (!persistentPerson.getEmail().equals(person.getEmail())) {
                String field = "person.nickname";
                errors.rejectValue(field, NICKNAME_ALREADY_EXISTS_ERROR_CODE, getMessage(USER_EXISTS_ERROR_MSG_KEY));
            }
        });
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
