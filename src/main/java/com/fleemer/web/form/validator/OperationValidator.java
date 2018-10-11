package com.fleemer.web.form.validator;

import com.fleemer.model.Account;
import com.fleemer.model.Operation;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class OperationValidator implements Validator {
    private static final String ACCOUNTS_EQUALS_ERROR = "operation.error.accounts-equals";
    private static final String INCOMPATIBLE_CURRENCY_TYPE_ERROR = "operation.error.incompatible-currency-type";

    private final MessageSource messageSource;

    @Autowired
    public OperationValidator(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Operation.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Operation operation = (Operation) target;
        if (operation.getCategory() != null) {
            return;
        }
        Account inAccount = operation.getInAccount();
        Account outAccount = operation.getOutAccount();
        if (inAccount.equals(outAccount)) {
            rejectValue(errors, ACCOUNTS_EQUALS_ERROR);
        }
        if (!inAccount.getCurrency().equals(outAccount.getCurrency())) {
            rejectValue(errors, INCOMPATIBLE_CURRENCY_TYPE_ERROR);
        }
    }

    private void rejectValue(Errors errors, String messageProperty) {
        Locale locale = LocaleContextHolder.getLocale();
        String msg = messageSource.getMessage(messageProperty, null, locale);
        errors.rejectValue("inAccount", "account.error", msg);
    }
}
