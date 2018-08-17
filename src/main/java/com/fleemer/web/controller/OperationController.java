package com.fleemer.web.controller;

import com.fleemer.model.Operation;
import com.fleemer.model.Person;
import com.fleemer.service.*;
import com.fleemer.service.exception.ServiceException;
import java.math.BigDecimal;
import java.security.Principal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.OptimisticLockException;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/")
public class OperationController {
    private static final String OPERATION_EDIT_VIEW = "operation_edit";
    private static final String PERSON_SESSION_ATTR = "person";
    private static final String ROOT_VIEW = "index";

    private final AccountService accountService;
    private final CategoryService categoryService;
    private final OperationService operationService;
    private final PersonService personService;

    @Autowired
    public OperationController(AccountService accountService, CategoryService categoryService, PersonService personService,
                               OperationService operationService) {
        this.accountService = accountService;
        this.categoryService = categoryService;
        this.personService = personService;
        this.operationService = operationService;
    }

    @GetMapping
    public String operations(Model model, HttpSession session, Principal principal) {
        model.addAttribute("operation", new Operation());
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        if (person == null) {
            Optional<Person> optional = personService.findByEmail(principal.getName());
            if (!optional.isPresent()) {
                return "redirect:/logout";
            }
            session.setAttribute(PERSON_SESSION_ATTR, optional.get());
        }
        BigDecimal totalBalance = accountService.getTotalBalance(person);
        model.addAttribute("totalBalance", totalBalance);
        model.addAttribute("accounts", accountService.findAll(person));
        return ROOT_VIEW;
    }

    @ResponseBody
    @GetMapping("/operations/json")
    public OperationPageDto operations(@RequestParam("page") int page, @RequestParam("size") int size,
                                       HttpSession session) {
        Pageable pageable = PageRequest.of(page, size, new Sort(Sort.Direction.DESC, "date"));
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        Page<Operation> operationPage = operationService.findAllByPerson(person, pageable);
        return new OperationPageDto(operationPage.getNumber(), operationPage.getTotalPages(), operationPage.getContent());
    }

    @ResponseBody
    @GetMapping("/operations/dailyvolumes/json")
    public List<DailyVolumesDto> operations(HttpSession session) {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonth().getValue();
        int lengthOfMonth = today.lengthOfMonth();
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate till = LocalDate.of(year, month, lengthOfMonth);
        List<Object[]> volumes = operationService.findAllDailyVolumes(from, till, person);
        return convertDailyVolumes(from, till, volumes);
    }

    @PostMapping("/operations/create")
    public String create(@Valid @ModelAttribute Operation operation, BindingResult result, Model model,
                         HttpSession session) throws ServiceException {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        if (result.hasErrors()) {
            model.addAttribute("accounts", accountService.findAll(person));
            return ROOT_VIEW;
        }
        operationService.save(operation);
        return "redirect:/";
    }

    @GetMapping("/operations/update")
    public String update(@RequestParam("id") long id, Model model, HttpSession session) {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        Optional<Operation> operation = operationService.getByIdAndPerson(id, person);
        if (!operation.isPresent()) {
            return "redirect:/";
        }
        model.addAttribute("operation", operation.get());
        model.addAttribute("accounts", accountService.findAll(person));
        model.addAttribute("categories", categoryService.findAll(person));
        return OPERATION_EDIT_VIEW;
    }

    @PostMapping("/operations/update")
    public String update(@Valid @ModelAttribute("operation") Operation formOperation, BindingResult bindingResult,
                         Model model, HttpSession session) throws ServiceException {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        if (bindingResult.hasErrors()) {
            model.addAttribute("accounts", accountService.findAll(person));
            return OPERATION_EDIT_VIEW;
        }
        List<Operation> operations = operationService.findAllByPerson(person);
        if (!operations.contains(formOperation)) {
            return "redirect:/";
        }
        long id = formOperation.getId();
        Operation operation = operationService.findById(id).orElse(null);
        if (operation == null) {
            return "redirect:/";
        }
        String param = "";
        try {
            operationService.save(formOperation);
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
            param = "?error=lock";
        }
        return "redirect:/" + param;
    }

    @GetMapping("/operations/delete")
    public String delete(@RequestParam("id") long id, HttpSession session) {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        Optional<Operation> operation = operationService.getByIdAndPerson(id, person);
        if (!operation.isPresent()) {
            return "redirect:/";
        }
        operationService.delete(operation.get());
        return "redirect:/";
    }

    private List<DailyVolumesDto> convertDailyVolumes(LocalDate from, LocalDate till, List<Object[]> volumes) {
        List<DailyVolumesDto> result = new ArrayList<>();
        LocalDate curDate = from;
        for (Object[] volume : volumes) {
            LocalDate date = ((Date) volume[0]).toLocalDate();
            while (curDate.isBefore(date)) {
                result.add(new DailyVolumesDto(curDate, BigDecimal.ZERO, BigDecimal.ZERO));
                curDate = curDate.plusDays(1);
            }
            BigDecimal income = volume[1] != null ? (BigDecimal) volume[1] : null;
            BigDecimal outcome = volume[2] != null ? (BigDecimal) volume[2] : null;
            result.add(new DailyVolumesDto(date, income, outcome));
            curDate = curDate.plusDays(1);
        }
        while (curDate.isBefore(till)) {
            result.add(new DailyVolumesDto(curDate, BigDecimal.ZERO, BigDecimal.ZERO));
            curDate = curDate.plusDays(1);
        }
        return result;
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

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private class DailyVolumesDto {
        private LocalDate date;
        private BigDecimal income;
        private BigDecimal outcome;
    }
}
