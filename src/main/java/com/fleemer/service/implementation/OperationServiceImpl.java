package com.fleemer.service.implementation;

import com.fleemer.model.Account;
import com.fleemer.model.Category;
import com.fleemer.model.Operation;
import com.fleemer.model.Person;
import com.fleemer.model.enums.CategoryType;
import com.fleemer.model.enums.Currency;
import com.fleemer.repository.OperationRepository;
import com.fleemer.service.specification.OperationSpecification;
import com.fleemer.service.AccountService;
import com.fleemer.service.CategoryService;
import com.fleemer.service.OperationService;
import com.fleemer.service.exception.ServiceException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperationServiceImpl extends AbstractService<Operation, Long, OperationRepository>
        implements OperationService {
    private static final String SERVICE_EXCEPTION_TEMPLATE = "ServiceException: {}";
    private static final Logger logger = LoggerFactory.getLogger(OperationServiceImpl.class);

    private final AccountService accountService;
    private final CategoryService categoryService;
    private final OperationRepository repository;

    @Autowired
    public OperationServiceImpl(OperationRepository repository, AccountService accountService,
                                CategoryService categoryService) {
        this.repository = repository;
        this.accountService = accountService;
        this.categoryService = categoryService;
    }

    @Override
    protected OperationRepository getRepository() {
        return repository;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public long countOperationsByCategory(Category category) {
        return repository.countOperationsByCategory(category);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public long countOperationsByAccount(Account account) {
        return repository.countOperationsByInAccountOrOutAccount(account, account);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Operation> findAll(Person person) {
        return repository.findAllByInAccountPersonOrOutAccountPerson(person, person);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Operation> findAll(Person person, LocalDate from, LocalDate till) {
        return repository.findAllByInAccountPersonOrOutAccountPersonAndDateBetween(person, person, from, till);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Page<Operation> findAll(Person person, Pageable pageable, boolean orMode, @Nullable LocalDate from,
                                   @Nullable LocalDate till, @Nullable List<Account> inAccounts,
                                   @Nullable List<Account> outAccounts, @Nullable List<Category> categories,
                                   @Nullable BigDecimal min, @Nullable BigDecimal max, @Nullable String comment) {
        Specification<Operation> s = OperationSpecification.createSpecification(orMode, person, from, till, inAccounts,
                outAccounts, categories, min, max, comment);
        return repository.findAll(s, pageable);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Optional<Operation> findByIdAndPerson(Long id, Person person) {
        return repository.getByIdAndInAccountPersonOrOutAccountPerson(id, person, person);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Object[]> findAllDailyVolumes(Person person, Currency currency, LocalDate fromDate, LocalDate tillDate)
            throws ServiceException {
        checkDatesSequence(fromDate, tillDate);
        return repository.findAllDailyVolumes(currency.ordinal(), fromDate, tillDate, person);
    }

    @Override
    @Transactional
    public <S extends Operation> S save(S entity) throws ServiceException {
        Account in = entity.getInAccount();
        Account out = entity.getOutAccount();
        Category category = entity.getCategory();
        checkLogicalConstraints(in, out, category);
        BigDecimal sum = entity.getSum();
        if (entity.getId() != null) {
            Optional<Operation> optional = repository.findById(entity.getId());
            if (optional.isPresent()) {
                sum = sum.subtract(optional.get().getSum());
            }
        }
        transfer(in, out, category, sum);
        saveParents(entity, in, out, category);
        return super.save(entity);
    }

    @Override
    @Transactional
    public <S extends Operation> Iterable<S> saveAll(Iterable<S> entities) throws ServiceException {
        Map<String, Account> accounts = new HashMap<>();
        Map<String, Category> categories = new HashMap<>();
        entities.forEach(entity -> {
            Account inAccount = entity.getInAccount();
            if (inAccount != null) {
                accounts.put(inAccount.getName(), inAccount);
            }
            Account outAccount = entity.getOutAccount();
            if (outAccount != null) {
                accounts.put(outAccount.getName(), outAccount);
            }
            Category category = entity.getCategory();
            if (category != null) {
                categories.put(category.getName(), category);
            }
        });
        if (!accounts.isEmpty()) {
            accountService.saveAll(accounts.values());
        }
        if (!categories.isEmpty()) {
            categoryService.saveAll(categories.values());
        }
        for (Operation operation : entities) {
            this.save(operation);
        }
        return entities;
    }

    @Override
    @Transactional
    public void delete(Operation entity) throws ServiceException {
        Account in = entity.getInAccount();
        Account out = entity.getOutAccount();
        Category category = entity.getCategory();
        checkLogicalConstraints(in, out, category);
        BigDecimal sum = repository.getOne(entity.getId()).getSum().negate();
        transfer(in, out, category, sum);
        saveParents(entity, in, out, category);
        super.delete(entity);
    }

    private static void checkDatesSequence(LocalDate from, LocalDate till) throws ServiceException {
        if (from.isAfter(till)) {
            String msg = "Starting date is more than the ending date: from: " + from + ", till: " + till;
            logger.error(SERVICE_EXCEPTION_TEMPLATE, msg);
            throw new ServiceException(msg);
        }
    }

    private static void checkLogicalConstraints(Account in, Account out, Category cat) throws ServiceException {
        // Conditions: if all arguments are null or if all arguments are not null or if only one argument is not null
        // or in and out is the same.
        if ((in == null && out == null && cat == null) ||
                (in != null && out != null && cat != null) ||
                (in != null && out == null && cat == null || out != null && in == null && cat == null ||
                        cat != null && in == null && out == null)) {
            String msg = getIfNullErrorMsg(in, out, cat);
            logger.error(SERVICE_EXCEPTION_TEMPLATE, msg);
            throw new ServiceException(msg);
        }
        if (in == out) {
            String msg = "Accounts are equals: " + in + ", " + out;
            logger.error(SERVICE_EXCEPTION_TEMPLATE, msg);
            throw new ServiceException(msg);
        }
        // Category type is not suitable for the account
        if (cat != null) {
            CategoryType type = cat.getType();
            if ((in != null && type != CategoryType.INCOME) || (out != null && type != CategoryType.OUTCOME)) {
                String msg = "Wrong category type for such operation: " + type + ", " + in + ", " + out;
                logger.error(SERVICE_EXCEPTION_TEMPLATE, msg);
                throw new ServiceException(msg);
            }
        }
        // Accounts have different currencies during transfer operation
        if (in != null && out != null && !in.getCurrency().equals(out.getCurrency())) {
            String msg = "Accounts have different currencies: " + in + ", " + out;
            logger.error(SERVICE_EXCEPTION_TEMPLATE, msg);
            throw new ServiceException(msg);
        }
    }

    private static String getIfNullErrorMsg(Account in, Account out, Category cat) {
        return "There should be two nonzero parameters. Actually: " + in + ", " + out + ", " + cat;
    }

    private static void transfer(Account in, Account out, Category category, BigDecimal sum) {
        if (category == null) {
            in.setBalance(in.getBalance().add(sum));
            out.setBalance(out.getBalance().subtract(sum));
        }
        if (category != null && category.getType() == CategoryType.INCOME) {
            in.setBalance(in.getBalance().add(sum));
        }
        if (category != null && category.getType() == CategoryType.OUTCOME) {
            out.setBalance(out.getBalance().subtract(sum));
        }
    }

    private <S extends Operation> void saveParents(S entity, Account in, Account out, Category category)
            throws ServiceException {
        if (in != null) {
            entity.setInAccount(accountService.save(in));
        }
        if (out != null) {
            entity.setOutAccount(accountService.save(out));
        }
        if (category != null) {
            entity.setCategory(categoryService.save(category));
        }
    }
}
