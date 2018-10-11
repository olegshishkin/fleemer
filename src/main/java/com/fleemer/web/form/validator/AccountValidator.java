package com.fleemer.web.form.validator;

import com.fleemer.model.Account;
import com.fleemer.model.Person;
import com.fleemer.service.AccountService;
import java.util.Locale;
import java.util.Optional;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class AccountValidator implements Validator {
    private static final String ACCOUNT_EXISTS_ERROR_KEY = "accounts.error.name-exists";
    private static final String PERSON_SESSION_ATTR = "person";

    private final AccountService accountService;
    private final HttpSession session;
    private final MessageSource messageSource;

    @Autowired
    public AccountValidator(AccountService accountService, @Autowired(required = false) HttpSession session,
                            MessageSource messageSource) {
        this.accountService = accountService;
        this.session = session;
        this.messageSource = messageSource;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Account.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (session == null) {
            throw new IllegalArgumentException("No persons in current session");
        }
        Account account = (Account) target;
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        Optional<Account> optional = accountService.findByNameAndPerson(account.getName(), person);
        if (optional.isPresent()) {
            Account persistedAccount = optional.get();
            if (!persistedAccount.getId().equals(account.getId())) {
                Locale locale = LocaleContextHolder.getLocale();
                String msg = messageSource.getMessage(ACCOUNT_EXISTS_ERROR_KEY, null, locale);
                errors.rejectValue("name", "name.alreadyExists", msg);
            }
        }
    }
}
