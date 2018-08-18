package com.fleemer.web.controller;

import com.fleemer.model.Operation;
import com.fleemer.model.Person;
import com.fleemer.service.AccountService;
import com.fleemer.service.CategoryService;
import com.fleemer.service.OperationService;
import com.fleemer.service.exception.ServiceException;
import javax.persistence.OptimisticLockException;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/operations")
public class OperationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationController.class);
    private static final String OPERATION_UPDATE_VIEW = "operation_update";
    private static final String PERSON_SESSION_ATTR = "person";
    private static final String ROOT_VIEW = "operations";

    private final AccountService accountService;
    private final CategoryService categoryService;
    private final OperationService operationService;

    @Autowired
    public OperationController(AccountService accountService, CategoryService categoryService,
                               OperationService operationService) {
        this.accountService = accountService;
        this.categoryService = categoryService;
        this.operationService = operationService;
    }

    @GetMapping
    public ModelAndView operations() {
        return new ModelAndView(ROOT_VIEW);
    }

    @ResponseBody
    @GetMapping("/json")
    public OperationPageDto operations(@RequestParam("page") int page,
                                       @RequestParam("size") int size,
                                       @RequestParam(value = "from", required = false) String from,
                                       @RequestParam(value = "till", required = false) String till,
                                       HttpSession session) throws ServiceException {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        Pageable pageable = PageRequest.of(page, size, new Sort(Sort.Direction.DESC, "date"));
        LocalDate fromDate = from != null ? LocalDate.parse(from) : null;
        LocalDate tillDate = till != null ? LocalDate.parse(till) : null;
        Page<Operation> operationPage = operationService.findAllByPerson(person, fromDate, tillDate, pageable);
        return new OperationPageDto(operationPage.getNumber(), operationPage.getTotalPages(), operationPage.getContent());
    }

    @ResponseBody
    @GetMapping("/dailyvolumes/json")
    public List<DailyVolumesDto> dailyVolumes(HttpSession session) {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonth().getValue();
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate till = LocalDate.of(year, month, today.lengthOfMonth());
        List<Object[]> volumes = operationService.findAllDailyVolumes(from, till, person);
        return convertDailyVolumes(from, till, volumes);
    }

    @PostMapping("/create")
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

    @GetMapping("/update")
    public String update(@RequestParam("id") long id, Model model, HttpSession session,
                         @RequestParam("redirect") String url) {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        Optional<Operation> operation = operationService.getByIdAndPerson(id, person);
        if (!operation.isPresent()) {
            return "redirect:" + url;
        }
        model.addAttribute("accounts", accountService.findAll(person));
        model.addAttribute("categories", categoryService.findAll(person));
        model.addAttribute("operation", operation.get());
        model.addAttribute("redirectUrl", url);
        return OPERATION_UPDATE_VIEW;
    }

    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("operation") Operation formOperation, BindingResult bindingResult,
                         Model model, HttpSession session, @RequestParam("redirect") String url)
            throws ServiceException {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        if (bindingResult.hasErrors()) {
            model.addAttribute("accounts", accountService.findAll(person));
            return OPERATION_UPDATE_VIEW;
        }
        List<Operation> operations = operationService.findAllByPerson(person);
        if (!operations.contains(formOperation)) {
            return "redirect:" + url;
        }
        long id = formOperation.getId();
        Operation operation = operationService.findById(id).orElse(null);
        if (operation == null) {
            return "redirect:" + url;
        }
        String param = "";
        try {
            operationService.save(formOperation);
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
            LOGGER.warn("Optimistic lock: {}", e.getMessage());
            param = "?error=lock";
        }
        return "redirect:" + url + param;
    }

    @GetMapping("/delete")
    public String delete(@RequestParam("id") long id, HttpSession session, @RequestParam("redirect") String url) {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        Optional<Operation> operation = operationService.getByIdAndPerson(id, person);
        if (!operation.isPresent()) {
            return "redirect:" + url;
        }
        operationService.delete(operation.get());
        return "redirect:" + url;
    }

    private List<DailyVolumesDto> convertDailyVolumes(LocalDate from, LocalDate till, List<Object[]> volumes) {
        List<DailyVolumesDto> dto = new ArrayList<>();
        LocalDate curDate = from;
        for (Object[] volume : volumes) {
            LocalDate date = ((Date) volume[0]).toLocalDate();
            while (curDate.isBefore(date)) {
                dto.add(new DailyVolumesDto(curDate, BigDecimal.ZERO, BigDecimal.ZERO));
                curDate = curDate.plusDays(1);
            }
            BigDecimal income = volume[1] != null ? (BigDecimal) volume[1] : null;
            BigDecimal outcome = volume[2] != null ? (BigDecimal) volume[2] : null;
            dto.add(new DailyVolumesDto(date, income, outcome));
            curDate = curDate.plusDays(1);
        }
        while (curDate.isBefore(till)) {
            dto.add(new DailyVolumesDto(curDate, BigDecimal.ZERO, BigDecimal.ZERO));
            curDate = curDate.plusDays(1);
        }
        return dto;
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
