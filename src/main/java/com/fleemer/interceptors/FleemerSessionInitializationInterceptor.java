package com.fleemer.interceptors;

import com.fleemer.model.Account;
import com.fleemer.model.Person;
import com.fleemer.model.enums.Currency;
import com.fleemer.security.PersonDetails;
import com.fleemer.service.AccountService;
import com.fleemer.service.PersonService;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Component
public class FleemerSessionInitializationInterceptor extends HandlerInterceptorAdapter {
    private static final String EN_LANG = "en";
    private static final String PERSON_ATTR = "person";
    private static final String RU_LANG = "ru";
    private static final String SWITCH_LOCALE_ATTR = "switchLocale";
    private static final String TOTAL_BALANCES_ATTR = "totalBalances";

    private final AccountService accountService;
    private final PersonService personService;

    @Autowired
    public FleemerSessionInitializationInterceptor(AccountService accountService, PersonService personService) {
        this.accountService = accountService;
        this.personService = personService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession();
        String switchLocale = (String) session.getAttribute(SWITCH_LOCALE_ATTR);
        if (switchLocale == null) {
            session.setAttribute(SWITCH_LOCALE_ATTR, getSwitchLocale());
        }
        Person person = (Person) session.getAttribute(PERSON_ATTR);
        if (person == null) {
            person = getPerson();
            session.setAttribute(PERSON_ATTR, person);
        }
        List<Account> accounts = accountService.findAll(person);
        session.setAttribute(TOTAL_BALANCES_ATTR, getTotalBalances(accounts));
        return true;
    }

    private Person getPerson() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Second condition is necessary, because the getPrincipal() method returns a String rather than UserDetails
        // when anonymous user is present
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return null;
        }
        String email = ((PersonDetails) authentication.getPrincipal()).getUsername();
        return personService.findByEmail(email).orElse(null);
    }

    private String getSwitchLocale() {
        String language = LocaleContextHolder.getLocale().getLanguage();
        return language.equals(EN_LANG) ? RU_LANG : EN_LANG;
    }

    private static Map<Currency, BigDecimal> getTotalBalances(List<Account> accounts) {
        Map<Currency, BigDecimal> map = new HashMap<>();
        for (Account account : accounts) {
            Currency currency = account.getCurrency();
            BigDecimal accumulatedBalance = map.get(currency);
            if (accumulatedBalance == null) {
                map.put(currency, account.getBalance());
            } else {
                map.put(currency, accumulatedBalance.add(account.getBalance()));
            }
        }
        return map;
    }
}
