package com.fleemer.service.implementation;

import com.fleemer.model.Account;
import com.fleemer.model.Category;
import com.fleemer.model.Operation;
import com.fleemer.model.Person;
import com.fleemer.model.enums.CategoryType;
import com.fleemer.repository.OperationRepository;
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
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperationServiceImpl extends AbstractService<Operation, Long, OperationRepository>
        implements OperationService {
    private static final String DATES_SEQUENCE_ERROR = "Starting date is more than the ending date";
    private static final String NO_ACCOUNTS_AND_CATEGORY_ERROR = "The category and at least one account are missing.";
    private static final String NO_ACCOUNTS_ERROR = "Both the income account and outcome account are missing.";
    private static final String NO_OPERATION_TYPE_ERROR = "Category and both the accounts are not null. There is " +
            "no way to determine an operation type.";
    private static final String SERVICE_EXCEPTION_TEMPLATE = "ServiceException: {}";
    private static final String WRONG_CATEGORY_TYPE_ERROR = "Wrong category type for that operation: ";
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
    @Transactional(readOnly = true)
    public List<Operation> findAllByPerson(Person person, @Nullable LocalDate from, @Nullable LocalDate till)
            throws ServiceException {
        if (from == null & till == null) {
            return repository.findAllByInAccountPersonOrOutAccountPerson(person, person);
        }
        if (from != null & till != null) {
            checkDatesSequence(from, till);
            return repository.findAllByInAccountPersonOrOutAccountPersonAndDateBetween(person, person, from, till);
        }
        String msg = "Missing one of the dates. From: " + from + ". Till: " + till + '.';
        logger.error(SERVICE_EXCEPTION_TEMPLATE, msg);
        throw new ServiceException(msg);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Operation> findAllByPerson(Person person, @Nullable LocalDate from, @Nullable LocalDate till,
                                           Pageable pageable) throws ServiceException {
        if (from == null || till == null) {
            return repository.findAllByInAccountPersonOrOutAccountPerson(person, person, pageable);
        }
        checkDatesSequence(from, till);
        return repository.findAllByInAccountPersonOrOutAccountPersonAndDateBetween(person, person, from, till, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Operation> findByIdAndPerson(Long id, Person person) {
        return repository.getByIdAndInAccountPersonOrOutAccountPerson(id, person, person);
    }

    @Override
    @Transactional(readOnly = true)
    public long countOperationsByCategory(Category category) {
        return repository.countOperationsByCategory(category);
    }

    @Override
    @Transactional(readOnly = true)
    public long countOperationsByAccount(Account account) {
        return repository.countOperationsByInAccountOrOutAccount(account, account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> findAllDailyVolumes(LocalDate fromDate, LocalDate tillDate, Person person) {
        return repository.findAllDailyVolumes(fromDate, tillDate, person);
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
            String msg = DATES_SEQUENCE_ERROR + ": from: " + from + ", till: " + till;
            logger.error(SERVICE_EXCEPTION_TEMPLATE, msg);
            throw new ServiceException(msg);
        }
    }

    private static void checkLogicalConstraints(Account in, Account out, Category cat) throws ServiceException {
        if (in == null & out == null) {
            logger.error(SERVICE_EXCEPTION_TEMPLATE, NO_ACCOUNTS_ERROR);
            throw new ServiceException(NO_ACCOUNTS_ERROR);
        }
        if (cat == null & (in == null || out == null)) {
            logger.error(SERVICE_EXCEPTION_TEMPLATE, NO_ACCOUNTS_AND_CATEGORY_ERROR);
            throw new ServiceException(NO_ACCOUNTS_AND_CATEGORY_ERROR);
        }
        if (cat == null) {
            return;
        }
        if (in != null && out != null) {
            logger.error(SERVICE_EXCEPTION_TEMPLATE, NO_OPERATION_TYPE_ERROR);
            throw new ServiceException(NO_OPERATION_TYPE_ERROR);
        }
        CategoryType type = cat.getType();
        if ((in != null && type != CategoryType.INCOME) || (out != null && type != CategoryType.OUTCOME)) {
            String msg = WRONG_CATEGORY_TYPE_ERROR + type + '.';
            logger.error(SERVICE_EXCEPTION_TEMPLATE, msg);
            throw new ServiceException(msg);
        }
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
