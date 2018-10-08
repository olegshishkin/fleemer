package com.fleemer.web.controller;

import com.fleemer.model.Account;
import com.fleemer.model.Operation;
import com.fleemer.model.Person;
import com.fleemer.model.enums.Currency;
import com.fleemer.service.*;
import java.time.LocalDate;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/")
public class IndexController {
    private static final String PERSON_SESSION_ATTR = "person";
    private static final String ROOT_VIEW = "index";

    private final AccountService accountService;
    private final OperationService operationService;

    @Autowired
    public IndexController(AccountService accountService, OperationService operationService) {
        this.accountService = accountService;
        this.operationService = operationService;
    }

    @GetMapping
    public String index(Model model, HttpSession session) {
        model.addAttribute("operation", new Operation());
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        List<Account> accounts = accountService.findAll(person);
        model.addAttribute("accounts", accounts);
        LocalDate today = LocalDate.now();
        model.addAttribute("operations", operationService.findAll(person, today, today));
        model.addAttribute("currencies", Currency.values());
        return ROOT_VIEW;
    }
}
