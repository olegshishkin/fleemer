package com.fleemer.web.controller;

import com.fleemer.model.Account;
import com.fleemer.model.Person;
import com.fleemer.model.enums.AccountType;
import com.fleemer.model.enums.Currency;
import com.fleemer.service.AccountService;
import com.fleemer.service.PersonService;
import com.fleemer.service.exception.ServiceException;
import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/accounts")
public class AccountController {
    private static final String ROOT_VIEW = "accounts";
    private static final String ACCOUNT_EXISTS_ERROR_MSG_KEY = "accounts.error.name-exists";
    private final AccountService accountService;
    private final MessageSource messageSource;
    private final PersonService personService;

    @Autowired
    public AccountController(AccountService accountService, PersonService personService, MessageSource messageSource) {
        this.accountService = accountService;
        this.personService = personService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String accounts(Model model, Principal principal) {
        Person person = getCurrentPerson(principal);
        fillModel(model, accountService.findAll(person));
        model.addAttribute("account", new Account());
        return ROOT_VIEW;
    }

    @ResponseBody
    @GetMapping("/json")
    public List<Account> accounts(Principal principal) {
        Person person = getCurrentPerson(principal);
        return accountService.findAll(person);
    }

    @PostMapping("/create")
    public String newAccount(@Valid @ModelAttribute Account account, BindingResult bindingResult, Model model,
                             Principal principal) throws ServiceException {
        Person person = getCurrentPerson(principal);
        if (bindingResult.hasErrors()) {
            fillModel(model, accountService.findAll(person));
            return ROOT_VIEW;
        }
        Optional<Account> lookedAccount = accountService.findByNameAndPerson(account.getName(), person);
        if (lookedAccount.isPresent()) {
            String message = messageSource.getMessage(ACCOUNT_EXISTS_ERROR_MSG_KEY, null, Locale.getDefault());
            bindingResult.rejectValue("name", "name.alreadyExists", message);
            fillModel(model, accountService.findAll(person));
            return ROOT_VIEW;
        }
        account.setPerson(person);
        accountService.save(account);
        return "redirect:/account";
    }

    private Person getCurrentPerson(@NotNull Principal principal) {
        return personService.findByEmail(principal.getName()).orElseThrow();
    }

    private void fillModel(@NotNull Model model, Iterable<Account> collection) {
        model.addAttribute("accounts", collection);
        model.addAttribute("accountTypes", AccountType.values());
        model.addAttribute("currencies", Currency.values());
    }
}
