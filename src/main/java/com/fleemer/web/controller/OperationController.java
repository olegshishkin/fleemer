package com.fleemer.web.controller;

import com.fleemer.model.Account;
import com.fleemer.model.Category;
import com.fleemer.model.Operation;
import com.fleemer.model.Person;
import com.fleemer.service.*;
import com.fleemer.service.exception.ServiceException;
import com.fleemer.web.form.OperationForm;
import java.security.Principal;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/")
public class OperationController {
    private static final String ROOT_VIEW = "index";
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final PersonService personService;
    private final OperationService operationService;

    @Autowired
    public OperationController(AccountService accountService, CategoryService categoryService, PersonService personService,
                               OperationService operationService) {
        this.accountService = accountService;
        this.categoryService = categoryService;
        this.personService = personService;
        this.operationService = operationService;
    }

    @GetMapping
    public String operations(Model model, Principal principal, HttpSession session) {
        model.addAttribute("operation", new OperationForm());
        Person person = getCurrentPerson(principal);
        session.setAttribute("userName", person.getFirstName());
        fillModel(model, person);
        return ROOT_VIEW;
    }

    @ResponseBody
    @GetMapping("/operations/json")
    public OperationPageDto operations(@RequestParam("page") int page, @RequestParam("size") int size,
                                       Principal principal) {
        Person person = getCurrentPerson(principal);
        Pageable pageable = PageRequest.of(page, size, new Sort(Sort.Direction.DESC, "date"));
        Page<Operation> operationPage = operationService.findAll(person, pageable);
        return new OperationPageDto(operationPage.getNumber(), operationPage.getTotalPages(), operationPage.getContent());
    }

    @PostMapping("/operations/create")
    public String create(@Valid @ModelAttribute OperationForm form, BindingResult result, Model model,
                               Principal principal) throws ServiceException {
        Person person = getCurrentPerson(principal);
        fillModel(model, person);
        if (result.hasErrors()) {
            return ROOT_VIEW;
        }
        Operation operation = getFilledOperation(form, person);
        operationService.save(operation);
        return "redirect:/";
    }

    @ResponseBody
    @GetMapping("/balance")
    public double balance(Principal principal) {
        Person person = getCurrentPerson(principal);
        return accountService.getTotalBalance(person).doubleValue();
    }

    private Person getCurrentPerson(@NotNull Principal principal) {
        return personService.findByEmail(principal.getName()).orElseThrow();
    }

    private void fillModel(@NotNull Model model, Person person) {
        model.addAttribute("accounts", accountService.findAll(person));
        model.addAttribute("operations", operationService.findAll(person));
    }

    private Operation getFilledOperation(@NotNull OperationForm form, Person person) {
        Account inAccount = accountService.findByNameAndPerson(form.getInAccountName(), person).orElse(null);
        Account outAccount = accountService.findByNameAndPerson(form.getOutAccountName(), person).orElse(null);
        Category category = categoryService.findByNameAndPerson(form.getCategoryName(), person).orElse(null);
        Operation operation = form.getOperation();
        operation.setInAccount(inAccount);
        operation.setOutAccount(outAccount);
        operation.setCategory(category);
        return operation;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private class OperationPageDto {
        private int currentPage;
        private int totalPages;
        private List<Operation> operations;
    }
}
