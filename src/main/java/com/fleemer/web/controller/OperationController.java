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
import com.fleemer.service.BaseService;
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
import java.time.temporal.ChronoUnit;
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

@Controller
@RequestMapping("/operations")
public class OperationController {
    private static final String CHARSET_NAME = "UTF-8";
    private static final String EMPTY_STRING = "";
    private static final String ILLEGAL_VALUES_EXCEPTION_MSG = "Same entities have different field(s) value(s): " +
            "1) %s, 2) %s.";
    private static final String IO_ERROR_EXCEPTION_TEMPLATE = "IO Error: Exception: {}";
    private static final String OPERATION_UPDATE_VIEW = "operation_update";
    private static final String PERSON_SESSION_ATTR = "person";
    private static final String REDIRECT = "redirect:";
    private static final String ROOT_VIEW = "operations";
    private static final Logger logger = LoggerFactory.getLogger(OperationController.class);

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
    public String operations(@RequestParam(value = "account", required = false) Long accountId, Model model,
                             HttpSession session) {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        model.addAttribute("accounts", accountService.findAll(person));
        model.addAttribute("categories", categoryService.findAll(person));
        model.addAttribute("accountId", accountId);
        return ROOT_VIEW;
    }

    @ResponseBody
    @PostMapping("/json")
    public OperationPageDTO operations(@RequestParam("page") int page,
                                       @RequestParam("size") int size,
                                       @RequestParam(value = "orMode", required = false) boolean orMode,
                                       @RequestParam(value = "inAccounts", required = false) List<Long> inAccountsId,
                                       @RequestParam(value = "outAccounts", required = false) List<Long> outAccountsId,
                                       @RequestParam(value = "categories", required = false) List<Long> categoriesId,
                                       @RequestParam(value = "minSum", required = false) BigDecimal min,
                                       @RequestParam(value = "maxSum", required = false) BigDecimal max,
                                       @RequestParam(value = "comment", required = false) String comment,
                                       @RequestParam(value = "from", required = false) String from,
                                       @RequestParam(value = "till", required = false) String till,
                                       HttpSession session) throws ServiceException {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        Pageable pageable = PageRequest.of(page, size, new Sort(Sort.Direction.DESC, "date"));
        LocalDate fromDate = EMPTY_STRING.equals(from) ? null : LocalDate.parse(from);
        LocalDate tillDate = EMPTY_STRING.equals(till) ? null : LocalDate.parse(till);
        String readyComment = EMPTY_STRING.equals(comment) ? null : comment;
        List<Account> inAccounts = inAccountsId.isEmpty() ? null : findAllByIds(accountService, inAccountsId);
        List<Account> outAccounts = outAccountsId.isEmpty() ? null : findAllByIds(accountService, outAccountsId);
        List<Category> categories = categoriesId.isEmpty() ? null : findAllByIds(categoryService, categoriesId);
        Page<Operation> operationPage = operationService.findAll(person, pageable, orMode, fromDate, tillDate,
                inAccounts, outAccounts, categories, min, max, readyComment);
        int pageNumber = operationPage.getNumber();
        return new OperationPageDTO(pageNumber, operationPage.getTotalPages(), operationPage.getContent());
    }

    @ResponseBody
    @GetMapping("/dailyVolumes/json")
    public List<DailyVolumesDTO> dailyVolumes(@RequestParam("currency") com.fleemer.model.enums.Currency currency,
                                              @RequestParam(value = "from", required = false) String from,
                                              @RequestParam(value = "till", required = false) String till,
                                              HttpSession session) throws ServiceException {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        LocalDate[] dateRangeBounds = getDateRangeBounds(from, till);
        LocalDate fromDate = dateRangeBounds[0];
        LocalDate tillDate = dateRangeBounds[1];
        List<Object[]> volumes = operationService.findAllDailyVolumes(person, currency, fromDate, tillDate);
        return convertDailyVolumes(fromDate, tillDate, volumes);
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
        List<Operation> operations = operationService.findAll(person);
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
            logger.warn("Optimistic lock: {}", e.getMessage());
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
    public void exportXml(@RequestParam(value = "from", required = false, defaultValue = "0000-01-01") String from,
                          @RequestParam(value = "till", required = false, defaultValue = "9999-12-31") String till,
                          HttpSession session, HttpServletResponse response) throws ServiceException, IOException {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        List<Operation> operations = operationService.findAll(person, LocalDate.parse(from), LocalDate.parse(till));
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
            logger.warn(IO_ERROR_EXCEPTION_TEMPLATE, e.getMessage());
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
            logger.warn(IO_ERROR_EXCEPTION_TEMPLATE, e.getMessage());
            throw e;
        }
        return REDIRECT + "/options/serialize?success";
    }

    private List<DailyVolumesDTO> convertDailyVolumes(LocalDate from, LocalDate till, List<Object[]> volumes) {
        boolean isListEmpty = volumes.isEmpty();
        LocalDate now = LocalDate.now();
        LocalDate firstExistedDate = isListEmpty ? now : ((Date) volumes.get(0)[0]).toLocalDate();
        LocalDate lastExistedDate = isListEmpty ? now : ((Date) volumes.get(volumes.size() - 1)[0]).toLocalDate();
        List<DailyVolumesDTO> dto = new ArrayList<>();
        LocalDate curDate = getLimitedDate(from, firstExistedDate, 30, firstExistedDate, 1);
        for (Object[] volume : volumes) {
            LocalDate date = ((Date) volume[0]).toLocalDate();
            while (curDate.isBefore(date)) {
                dto.add(new DailyVolumesDTO(curDate, BigDecimal.ZERO, BigDecimal.ZERO));
                curDate = curDate.plusDays(1);
            }
            BigDecimal income = volume[1] != null ? (BigDecimal) volume[1] : null;
            BigDecimal outcome = volume[2] != null ? (BigDecimal) volume[2] : null;
            dto.add(new DailyVolumesDTO(date, income, outcome));
            curDate = curDate.plusDays(1);
        }
        int lastMonthDay = lastExistedDate.lengthOfMonth();
        LocalDate outOfBoundDate = getLimitedDate(till, lastExistedDate, 30, lastExistedDate, lastMonthDay).plusDays(1);
        while (curDate.isBefore(outOfBoundDate)) {
            dto.add(new DailyVolumesDTO(curDate, BigDecimal.ZERO, BigDecimal.ZERO));
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

    private static LocalDate[] getDateRangeBounds(String start, String end) {
        LocalDate today = LocalDate.now();
        LocalDate from = start == null || start.isEmpty() ? null : LocalDate.parse(start);
        LocalDate till = end == null || end.isEmpty() ? null : LocalDate.parse(end);
        if (from != null && till == null) {
            till = LocalDate.of(today.getYear(), today.getMonth().getValue(), today.lengthOfMonth());
            if (from.isAfter(till)) {
                till = LocalDate.of(from.getYear(), from.getMonth().getValue(), from.lengthOfMonth());
            }
        }
        if (from == null && till != null) {
            from = LocalDate.of(today.getYear(), today.getMonth().getValue(), 1);
            if (from.isAfter(till)) {
                from = LocalDate.of(till.getYear(), till.getMonth().getValue(), 1);
            }
        }
        if ((from == null && till == null) || (from.isAfter(till))) {
            from = LocalDate.of(today.getYear(), today.getMonth().getValue(), 1);
            till = LocalDate.of(today.getYear(), today.getMonth().getValue(), today.lengthOfMonth());
        }
        return new LocalDate[]{from, till};
    }

    /**
     * Limits the dates range in case when dates difference is too far.
     * @param d1 first date for comparison
     * @param d2 second date for comparison
     * @param maxDaysDifference maximal allowable difference (in days) between dates
     * @param basedDate based date for the 'limited date' calculation
     * @param limitedDayOfMonth day of month for the limited date
     * @return if dates difference not exceeds limit, d1 will be returned. Otherwise limited date will be returned.
     */
    private static LocalDate getLimitedDate(LocalDate d1, LocalDate d2, long maxDaysDifference, LocalDate basedDate,
                                            int limitedDayOfMonth) {
        long difference = Math.abs(ChronoUnit.DAYS.between(d1, d2));
        if (difference > maxDaysDifference) {
            return LocalDate.of(basedDate.getYear(), basedDate.getMonth().getValue(), limitedDayOfMonth);
        } else {
            return d1;
        }
    }

    private static <T extends Serializable, ID extends Comparable<? super ID>> List<T> findAllByIds(BaseService<T, ID> service,
                                                                                                    Collection<ID> ids) {
        return ids == null ? null : (List<T>) service.findAllById(ids);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class OperationPageDTO {
        private int currentPage;
        private int totalPages;
        private List<Operation> operations;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class DailyVolumesDTO {
        private LocalDate date;
        private BigDecimal income;
        private BigDecimal outcome;
    }
}
