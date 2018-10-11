package com.fleemer.web.controller;

import com.fleemer.aop.LogAfterReturning;
import com.fleemer.model.Account;
import com.fleemer.model.Person;
import com.fleemer.model.enums.AccountType;
import com.fleemer.model.enums.Currency;
import com.fleemer.service.AccountService;
import com.fleemer.service.OperationService;
import com.fleemer.service.exception.ServiceException;
import com.fleemer.web.form.validator.AccountValidator;
import java.util.Optional;
import javax.persistence.OptimisticLockException;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/accounts")
public class AccountController {
    private static final String ACCOUNT_UPDATE_VIEW = "account_update";
    private static final String PERSON_SESSION_ATTR = "person";
    private static final String REDIRECT_ACCOUNTS_URL = "redirect:/accounts";
    private static final String ROOT_VIEW = "accounts";
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;
    private final OperationService operationService;
    private final AccountValidator accountValidator;

    @Autowired
    public AccountController(AccountService accountService, OperationService operationService,
                             AccountValidator accountValidator) {
        this.accountService = accountService;
        this.operationService = operationService;
        this.accountValidator = accountValidator;
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(accountValidator);
    }

    @GetMapping
    public String accounts(Model model, HttpSession session) {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        fillModel(model, accountService.findAll(person));
        model.addAttribute("account", new Account());
        return ROOT_VIEW;
    }

    @LogAfterReturning
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute Account account, BindingResult bindingResult, Model model,
                         HttpSession session) throws ServiceException {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        if (bindingResult.hasErrors()) {
            fillModel(model, accountService.findAll(person));
            return ROOT_VIEW;
        }
        account.setPerson(person);
        accountService.save(account);
        return REDIRECT_ACCOUNTS_URL;
    }

    @GetMapping("/update")
    public String update(@RequestParam("id") long id, Model model, HttpSession session) {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        Optional<Account> account = accountService.findByIdAndPerson(id, person);
        if (!account.isPresent()) {
            return REDIRECT_ACCOUNTS_URL;
        }
        model.addAttribute("account", account.get());
        model.addAttribute("accountTypes", AccountType.values());
        model.addAttribute("currencies", Currency.values());
        return ACCOUNT_UPDATE_VIEW;
    }

    @LogAfterReturning
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("account") Account formAccount, BindingResult bindingResult,
                         Model model, HttpSession session) throws ServiceException {
        if (bindingResult.hasErrors()) {
            model.addAttribute("accountTypes", AccountType.values());
            model.addAttribute("currencies", Currency.values());
            return ACCOUNT_UPDATE_VIEW;
        }
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        Optional<Account> optional = accountService.findByIdAndPerson(formAccount.getId(), person);
        if (!optional.isPresent()) {
            return REDIRECT_ACCOUNTS_URL;
        }
        formAccount.setPerson(optional.get().getPerson());
        try {
            accountService.save(formAccount);
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
            logger.warn("Optimistic lock: {}", e.getMessage());
            return REDIRECT_ACCOUNTS_URL + "?error=lock";
        }
        return REDIRECT_ACCOUNTS_URL + "?success";
    }

    @LogAfterReturning
    @GetMapping("/delete")
    public String delete(@RequestParam("id") long id, HttpSession session) throws ServiceException {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        Optional<Account> optional = accountService.findByIdAndPerson(id, person);
        if (optional.isPresent()) {
            Account account = optional.get();
            long operationsCount = operationService.countOperationsByAccount(account);
            if (operationsCount > 0) {
                return REDIRECT_ACCOUNTS_URL + "?deleteForbidden";
            }
            accountService.delete(account);
        }
        return REDIRECT_ACCOUNTS_URL;
    }

    private void fillModel(Model model, Iterable<Account> collection) {
        model.addAttribute("accounts", collection);
        model.addAttribute("accountTypes", AccountType.values());
        model.addAttribute("currencies", Currency.values());
    }
}
