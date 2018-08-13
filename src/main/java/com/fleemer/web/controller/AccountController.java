package com.fleemer.web.controller;

import com.fleemer.model.Account;
import com.fleemer.model.Operation;
import com.fleemer.model.Person;
import com.fleemer.model.enums.AccountType;
import com.fleemer.model.enums.Currency;
import com.fleemer.service.AccountService;
import com.fleemer.service.OperationService;
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
    private static final String ACCOUNT_EDIT_VIEW = "account_edit";
    private static final String ACCOUNT_EXISTS_ERROR_KEY = "accounts.error.name-exists";
    private static final String DELETING_FORBIDDEN_ERROR_KEY = "accounts.error.delete-forbidden";
    private static final String ROOT_VIEW = "accounts";

    private final AccountService accountService;
    private final MessageSource messageSource;
    private final OperationService operationService;
    private final PersonService personService;

    @Autowired
    public AccountController(AccountService accountService, PersonService personService, MessageSource messageSource,
                             OperationService operationService) {
        this.accountService = accountService;
        this.personService = personService;
        this.messageSource = messageSource;
        this.operationService = operationService;
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
    public String create(@Valid @ModelAttribute Account account, BindingResult bindingResult, Model model,
                             Principal principal) throws ServiceException {
        Person person = getCurrentPerson(principal);
        if (bindingResult.hasErrors()) {
            fillModel(model, accountService.findAll(person));
            return ROOT_VIEW;
        }
        Optional<Account> lookedAccount = accountService.findByNameAndPerson(account.getName(), person);
        if (lookedAccount.isPresent()) {
            String message = messageSource.getMessage(ACCOUNT_EXISTS_ERROR_KEY, null, Locale.getDefault());
            bindingResult.rejectValue("name", "name.alreadyExists", message);
            fillModel(model, accountService.findAll(person));
            return ROOT_VIEW;
        }
        account.setPerson(person);
        accountService.save(account);
        return "redirect:/accounts";
    }

    @GetMapping("/update")
    public String update(@RequestParam("id") long id, Model model, Principal principal) {
        Person person = getCurrentPerson(principal);
        Account account = accountService.findById(id).orElseThrow();
        if (!isOwned(person, account)) {
            return "redirect:/accounts";
        }
        model.addAttribute("account", account);
        model.addAttribute("accountTypes", AccountType.values());
        model.addAttribute("currencies", Currency.values());
        return ACCOUNT_EDIT_VIEW;
    }

    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("account") Account formAccount, BindingResult bindingResult,
                         Model model, Principal principal) throws ServiceException {
        if (bindingResult.hasErrors()) {
            model.addAttribute("accountTypes", AccountType.values());
            return ACCOUNT_EDIT_VIEW;
        }
        Person person = getCurrentPerson(principal);
        Optional<Account> optional = accountService.getByIdAndPerson(formAccount.getId(), person);
        if (!optional.isPresent()) {
            return "redirect:/accounts";
        }
        Account account = optional.get();
        if (!canUseName(account, formAccount, person)) {
            bindingResult.rejectValue("name", "name.alreadyExists", getMessage(ACCOUNT_EXISTS_ERROR_KEY));
            fillModel(model, accountService.findAll(person));
            return ACCOUNT_EDIT_VIEW;
        }
        account.setName(formAccount.getName());
        account.setType(formAccount.getType());
        account.setCurrency(formAccount.getCurrency());
        account.setBalance(formAccount.getBalance());
        accountService.save(account);
        return "redirect:/accounts";
    }

    @GetMapping("/delete")
    public String delete(@RequestParam("id") long id, Model model, Principal principal) {
        Person person = getCurrentPerson(principal);
        Optional<Account> optional = accountService.getByIdAndPerson(id, person);
        if (optional.isPresent()) {
            Account account = optional.get();
            List<Operation> relatedOperations = operationService.findAllByAccount(account);
            if (!relatedOperations.isEmpty()) {
                fillModel(model, accountService.findAll(person));
                model.addAttribute("error", getMessage(DELETING_FORBIDDEN_ERROR_KEY));
                model.addAttribute("account", new Account());
                return ROOT_VIEW;
            }
            accountService.delete(account);
        }
        return "redirect:/accounts";
    }

    private boolean isOwned(Person person, Account account) {
        return account.getPerson().equals(person);
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
        return messageSource.getMessage(key, null, Locale.getDefault());
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
