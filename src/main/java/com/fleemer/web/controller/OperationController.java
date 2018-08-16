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
    private static final String OPERATION_EDIT_VIEW = "operation_edit";
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
    public String operations(Model model, Principal principal, HttpSession session) {
        model.addAttribute("operation", new Operation());
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
        Page<Operation> operationPage = operationService.findAllByPerson(person, pageable);
        return new OperationPageDto(operationPage.getNumber(), operationPage.getTotalPages(), operationPage.getContent());
    }

    @ResponseBody
    @GetMapping("/operations/dailyvolumes/json")
    public List<DailyVolumesDto> operations(Principal principal) {
        Person person = getCurrentPerson(principal);
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
                         Principal principal) throws ServiceException {
        Person person = getCurrentPerson(principal);
        fillModel(model, person);
        if (result.hasErrors()) {
            return ROOT_VIEW;
        }
        operationService.save(operation);
        return "redirect:/";
    }

    @GetMapping("/operations/update")
    public String update(@RequestParam("id") long id, Model model, Principal principal) {
        Person person = getCurrentPerson(principal);
        Operation operation = operationService.findById(id).orElse(null);
        if (operation == null || !isOwned(person, operation)) {
            return "redirect:/";
        }
        model.addAttribute("operation", operation);
        model.addAttribute("accounts", accountService.findAll(person));
        model.addAttribute("categories", categoryService.findAll(person));
        return OPERATION_EDIT_VIEW;
    }

    @PostMapping("/operations/update")
    public String update(@Valid @ModelAttribute("operation") Operation formOperation, BindingResult bindingResult,
                         Model model, Principal principal) throws ServiceException {
        Person person = getCurrentPerson(principal);
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
        if (operation != null) {
            operation.setInAccount(formOperation.getInAccount());
            operation.setOutAccount(formOperation.getOutAccount());
            operation.setCategory(formOperation.getCategory());
            operation.setDate(formOperation.getDate());
            operation.setSum(formOperation.getSum());
            operation.setComment(formOperation.getComment());
            operationService.save(operation);
        }
        return "redirect:/";
    }

    @GetMapping("/operations/delete")
    public String delete(@RequestParam("id") long id, Principal principal) {
        Person person = getCurrentPerson(principal);
        Operation operation = operationService.findById(id).orElse(null);
        if (operation == null || !isOwned(person, operation)) {
            return "redirect:/";
        }
        operationService.delete(operation);
        return "redirect:/";
    }

    @ResponseBody
    @GetMapping("/balance")
    public double balance(Principal principal) {
        Person person = getCurrentPerson(principal);
        return accountService.getTotalBalance(person).doubleValue();
    }

    private boolean isOwned(Person person, Operation operation) {
        return operationService.findAllByPerson(person).contains(operation);
    }

    private Person getCurrentPerson(@NotNull Principal principal) {
        return personService.findByEmail(principal.getName()).orElseThrow();
    }

    private void fillModel(@NotNull Model model, Person person) {
        model.addAttribute("accounts", accountService.findAll(person));
        model.addAttribute("operations", operationService.findAllByPerson(person));
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
