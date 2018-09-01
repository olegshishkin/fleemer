package com.fleemer.web.controller;

import com.fleemer.model.Operation;
import com.fleemer.model.Person;
import com.fleemer.service.*;
import com.fleemer.service.exception.ServiceException;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Optional;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
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
    private final PersonService personService;

    @Autowired
    public IndexController(AccountService accountService, PersonService personService,
                           OperationService operationService) {
        this.accountService = accountService;
        this.personService = personService;
        this.operationService = operationService;
    }

    @GetMapping
    public String index(Model model, HttpSession session, Principal principal) throws ServiceException {
        model.addAttribute("operation", new Operation());
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        if (person == null) {
            Optional<Person> optional = personService.findByEmail(principal.getName());
            if (!optional.isPresent()) {
                return "redirect:/logout";
            }
            person = optional.get();
            session.setAttribute(PERSON_SESSION_ATTR, person);
        }
        if (session.getAttribute("switchLocale") == null) {
            String language = LocaleContextHolder.getLocale().getLanguage();
            session.setAttribute("switchLocale", language.equals("en") ? "ru" : "en");
        }
        model.addAttribute("accounts", accountService.findAll(person));
        BigDecimal totalBalance = accountService.getTotalBalance(person);
        model.addAttribute("totalBalance", totalBalance);
        LocalDate today = LocalDate.now();
        model.addAttribute("operations", operationService.findAllByPerson(person, today, today));
        return ROOT_VIEW;
    }
}
