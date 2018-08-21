package com.fleemer.web.controller;

import com.fleemer.model.Account;
import com.fleemer.model.Person;
import com.fleemer.model.enums.AccountType;
import com.fleemer.model.enums.Currency;
import com.fleemer.service.AccountService;
import com.fleemer.service.OperationService;
import com.fleemer.service.PersonService;
import com.fleemer.service.exception.ServiceException;
import java.security.Principal;
import java.util.Optional;
import javax.persistence.OptimisticLockException;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/accounts")
public class AccountController {
    private static final String ACCOUNT_UPDATE_VIEW = "account_update";
    private static final String ACCOUNT_EXISTS_ERROR_KEY = "accounts.error.name-exists";
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);
    private static final String PERSON_SESSION_ATTR = "person";
    private static final String ROOT_VIEW = "accounts";

    private final AccountService accountService;
    private final MessageSource messageSource;
    private final OperationService operationService;
    private final PersonService personService;

    @Autowired
    public AccountController(AccountService accountService, MessageSource messageSource,
                             OperationService operationService, PersonService personService) {
        this.accountService = accountService;
        this.messageSource = messageSource;
        this.operationService = operationService;
        this.personService = personService;
    }

    @GetMapping
    public String accounts(Model model, HttpSession session) {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        fillModel(model, accountService.findAll(person));
        model.addAttribute("account", new Account());
        return ROOT_VIEW;
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute Account account, BindingResult bindingResult, Model model,
                         Principal principal) throws ServiceException {
        Person person = personService.findByEmail(principal.getName()).orElseThrow();
        if (bindingResult.hasErrors()) {
            fillModel(model, accountService.findAll(person));
            return ROOT_VIEW;
        }
        Optional<Account> optional = accountService.findByNameAndPerson(account.getName(), person);
        if (optional.isPresent()) {
            bindingResult.rejectValue("name", "name.alreadyExists", getMessage(ACCOUNT_EXISTS_ERROR_KEY));
            fillModel(model, accountService.findAll(person));
            return ROOT_VIEW;
        }
        account.setPerson(person);
        accountService.save(account);
        return "redirect:/accounts";
    }

    @GetMapping("/update")
    public String update(@RequestParam("id") long id, Model model, HttpSession session) {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        Optional<Account> account = accountService.getByIdAndPerson(id, person);
        if (!account.isPresent()) {
            return "redirect:/accounts";
        }
        model.addAttribute("account", account.get());
        model.addAttribute("accountTypes", AccountType.values());
        model.addAttribute("currencies", Currency.values());
        return ACCOUNT_UPDATE_VIEW;
    }

    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("account") Account formAccount, BindingResult bindingResult,
                         Model model, HttpSession session) throws ServiceException {
        if (bindingResult.hasErrors()) {
            model.addAttribute("accountTypes", AccountType.values());
            return ACCOUNT_UPDATE_VIEW;
        }
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        Optional<Account> optional = accountService.getByIdAndPerson(formAccount.getId(), person);
        if (!optional.isPresent()) {
            return "redirect:/accounts";
        }
        Account account = optional.get();
        if (!canUseName(account, formAccount, person)) {
            bindingResult.rejectValue("name", "name.alreadyExists", getMessage(ACCOUNT_EXISTS_ERROR_KEY));
            fillModel(model, accountService.findAll(person));
            return ACCOUNT_UPDATE_VIEW;
        }
        formAccount.setPerson(account.getPerson());
        try {
            accountService.save(formAccount);
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
            LOGGER.warn("Optimistic lock: {}", e.getMessage());
            return "redirect:/accounts?error=lock";
        }
        return "redirect:/accounts?success";
    }

    @GetMapping("/delete")
    public String delete(@RequestParam("id") long id, HttpSession session) {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        Optional<Account> optional = accountService.getByIdAndPerson(id, person);
        if (optional.isPresent()) {
            Account account = optional.get();
            long operationsCount = operationService.countOperationsByAccounts(account);
            if (operationsCount > 0) {
                return "redirect:/accounts?deleteForbidden";
            }
            accountService.delete(account);
        }
        return "redirect:/accounts";
    }

    private boolean canUseName(Account account, Account formAccount, Person person) {
        String name = account.getName();
        String formName = formAccount.getName();
        if (name.equals(formName)) {
            return true;
        }
        return !accountService.findByNameAndPerson(formName, person).isPresent();
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    private void fillModel(@NotNull Model model, Iterable<Account> collection) {
        model.addAttribute("accounts", collection);
        model.addAttribute("accountTypes", AccountType.values());
        model.addAttribute("currencies", Currency.values());
    }
}
