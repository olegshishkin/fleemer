package com.fleemer.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fleemer.aop.LogAfterReturning;
import com.fleemer.model.Account;
import com.fleemer.model.Category;
import com.fleemer.model.Operation;
import com.fleemer.model.Person;
import com.fleemer.service.AccountService;
import com.fleemer.service.CategoryService;
import com.fleemer.service.OperationService;
import com.fleemer.service.exception.ServiceException;
import com.fleemer.web.other.OperationXmlMixIn;
import javax.persistence.OptimisticLockException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/operations")
public class OperationController {
    private static final String CHARSET_NAME = "UTF-8";
    private static final String ILLEGAL_VALUES_EXCEPTION_MSG = "Same entities have different field(s) value(s): " +
            "1) %s, 2) %s.";
    private static final String IO_ERROR_EXCEPTION_TEMPLATE = "IO Error: Exception: {}";
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationController.class);
    private static final String OPERATION_UPDATE_VIEW = "operation_update";
    private static final String PERSON_SESSION_ATTR = "person";
    private static final String REDIRECT = "redirect:";
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
        LocalDate fromDate = from == null || from.isEmpty() ? null : LocalDate.parse(from);
        LocalDate tillDate = till == null || till.isEmpty() ? null : LocalDate.parse(till);
        Page<Operation> operationPage = operationService.findAllByPerson(person, fromDate, tillDate, pageable);
        int pageNumber = operationPage.getNumber();
        return new OperationPageDto(pageNumber, operationPage.getTotalPages(), operationPage.getContent());
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

    @LogAfterReturning
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute Operation operation, BindingResult result, Model model,
                         HttpSession session) throws ServiceException {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        if (result.hasErrors()) {
            model.addAttribute("accounts", accountService.findAll(person));
            return ROOT_VIEW;
        }
        operationService.save(operation);
        return REDIRECT + "/";
    }

    @GetMapping("/update")
    public String update(@RequestParam("id") long id, Model model, HttpSession session,
                         @RequestParam("redirect") String url) {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        Optional<Operation> operation = operationService.findByIdAndPerson(id, person);
        if (!operation.isPresent()) {
            return REDIRECT + url;
        }
        model.addAttribute("accounts", accountService.findAll(person));
        model.addAttribute("categories", categoryService.findAll(person));
        model.addAttribute("operation", operation.get());
        model.addAttribute("redirectUrl", url);
        return OPERATION_UPDATE_VIEW;
    }

    @LogAfterReturning
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("operation") Operation formOperation, BindingResult bindingResult,
                         Model model, HttpSession session, @RequestParam("redirect") String url)
            throws ServiceException {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        if (bindingResult.hasErrors()) {
            model.addAttribute("accounts", accountService.findAll(person));
            return OPERATION_UPDATE_VIEW;
        }
        List<Operation> operations = operationService.findAllByPerson(person, null, null);
        if (!operations.contains(formOperation)) {
            return REDIRECT + url;
        }
        long id = formOperation.getId();
        Operation operation = operationService.findById(id).orElse(null);
        if (operation == null) {
            return REDIRECT + url;
        }
        String param = "";
        try {
            operationService.save(formOperation);
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
            LOGGER.warn("Optimistic lock: {}", e.getMessage());
            param = "?error=lock";
        }
        return REDIRECT + url + param;
    }

    @LogAfterReturning
    @GetMapping("/delete")
    public String delete(@RequestParam("id") long id, HttpSession session, @RequestParam("redirect") String url)
            throws ServiceException {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        Optional<Operation> operation = operationService.findByIdAndPerson(id, person);
        if (!operation.isPresent()) {
            return REDIRECT + url;
        }
        operationService.delete(operation.get());
        return REDIRECT + url;
    }

    @ResponseBody
    @GetMapping("/export")
    public void exportXml(@RequestParam(value = "from") String from, @RequestParam("till") String till,
                          HttpSession session, HttpServletResponse response) throws ServiceException, IOException {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        LocalDate fromDate = from == null || from.isEmpty() ? null : LocalDate.parse(from);
        LocalDate tillDate = till == null || till.isEmpty() ? null : LocalDate.parse(till);
        List<Operation> operations = operationService.findAllByPerson(person, fromDate, tillDate);
        response.setContentType("application/xml");
        response.setHeader("Content-Disposition", "attachment;filename=" + person.getEmail() + ".xml");
        try (ServletOutputStream out = response.getOutputStream()) {
            XmlMapper mapper = new XmlMapper();
            mapper.addMixIn(Operation.class, OperationXmlMixIn.class);
            mapper.addMixIn(Account.class, OperationXmlMixIn.class);
            mapper.addMixIn(Category.class, OperationXmlMixIn.class);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writer().withRootName("Operations").writeValue(out, operations);
        } catch (IOException e) {
            LOGGER.warn(IO_ERROR_EXCEPTION_TEMPLATE, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/import")
    public String importXml(@RequestParam MultipartFile file, HttpSession session) throws IOException,
            ServiceException {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        try (InputStream in = file.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, CHARSET_NAME))) {
            StringBuilder builder = new StringBuilder();
            String s;
            while ((s = reader.readLine()) != null) {
                builder.append(s);
            }
            ObjectMapper mapper = new XmlMapper();
            CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(List.class, Operation.class);
            List<Operation> operations = mapper.readValue(builder.toString(), collectionType);
            Map<String, Account> accounts = new HashMap<>();
            Map<String, Category> categories = new HashMap<>();
            for (Operation o : operations) {
                o.setInAccount(getFilledAccount(person, accounts, o.getInAccount()));
                o.setOutAccount(getFilledAccount(person, accounts, o.getOutAccount()));
                o.setCategory(getFilledCategory(person, categories, o.getCategory()));
            }
            operationService.saveAll(operations);
        } catch (IOException e) {
            LOGGER.warn(IO_ERROR_EXCEPTION_TEMPLATE, e.getMessage());
            throw e;
        }
        return REDIRECT + "/options/serialize?success";
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
        LocalDate outOfBoundDate = till.plusDays(1);
        while (curDate.isBefore(outOfBoundDate)) {
            dto.add(new DailyVolumesDto(curDate, BigDecimal.ZERO, BigDecimal.ZERO));
            curDate = curDate.plusDays(1);
        }
        return dto;
    }

    private Account getFilledAccount(Person person, Map<String, Account> cache, Account a) {
        if (a == null) {
            return null;
        }
        String name = a.getName();
        Account cached = cache.get(name);
        if (cached != null) {
            if (!cached.getName().equals(a.getName()) || !cached.getType().equals(a.getType()) ||
            !cached.getCurrency().equals(a.getCurrency())) {
                throw new IllegalArgumentException(String.format(ILLEGAL_VALUES_EXCEPTION_MSG, cached, a));
            }
            return cached;
        }
        Optional<Account> optional = accountService.findByNameAndPerson(name, person);
        if (optional.isPresent()) {
            Account touched = optional.get();
            touched.setType(a.getType());
            if (!touched.getCurrency().equals(a.getCurrency())) {
                throw new IllegalArgumentException(String.format(ILLEGAL_VALUES_EXCEPTION_MSG, touched, a));
            }
            cache.put(touched.getName(), touched);
            return touched;
        }
        a.setPerson(person);
        a.setBalance(BigDecimal.ZERO);
        cache.put(a.getName(), a);
        return a;
    }

    private Category getFilledCategory(Person person, Map<String, Category> cache, Category c) {
        if (c == null) {
            return null;
        }
        String name = c.getName();
        Category cached = cache.get(name);
        if (cached != null) {
            if (!cached.getName().equals(c.getName()) || !cached.getType().equals(c.getType())) {
                throw new IllegalArgumentException(String.format(ILLEGAL_VALUES_EXCEPTION_MSG, cached, c));
            }
            return cached;
        }
        Optional<Category> optional = categoryService.findByNameAndPerson(name, person);
        if (optional.isPresent()) {
            Category touched = optional.get();
            if (!touched.getType().equals(c.getType())) {
                throw new IllegalArgumentException(String.format(ILLEGAL_VALUES_EXCEPTION_MSG, touched, c));
            }
            cache.put(touched.getName(), touched);
            return touched;
        }
        c.setPerson(person);
        cache.put(c.getName(), c);
        return c;
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
