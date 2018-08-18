package com.fleemer.service.implementation;

import com.fleemer.model.Account;
import com.fleemer.model.Category;
import com.fleemer.model.Operation;
import com.fleemer.model.Person;
import com.fleemer.model.enums.CategoryType;
import com.fleemer.repository.OperationRepository;
import com.fleemer.service.OperationService;
import com.fleemer.service.exception.ServiceException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OperationServiceImpl extends AbstractService<Operation, Long, OperationRepository>
        implements OperationService {
    private static final String DATES_SEQUENCE_ERROR = "Starting date is more than the ending date";
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationServiceImpl.class);
    private static final String NO_ACCOUNTS_AND_CATEGORY_ERROR = "The category and at least one account are missing.";
    private static final String NO_ACCOUNTS_ERROR = "Both the income account and outcome account are missing.";
    private static final String NO_OPERATION_TYPE_ERROR = "Category and both the accounts are not null. There is " +
            "no way to determine an operation type.";
    private static final String WRONG_CATEGORY_TYPE_ERROR = "Wrong category type for that operation: ";

    private final OperationRepository repository;

    @Autowired
    public OperationServiceImpl(OperationRepository repository) {
        this.repository = repository;
    }

    @Override
    protected OperationRepository getRepository() {
        return repository;// todo transactional on class
    }

    @Override
    public List<Operation> findAllByPerson(Person person) {
        return repository.findAllByInAccountPersonOrOutAccountPerson(person, person);
    }

    @Override
    public Page<Operation> findAllByPerson(Person person, LocalDate from, LocalDate till, Pageable pageable)
            throws ServiceException {
        if (from == null || till == null) {
            return repository.findAllByInAccountPersonOrOutAccountPerson(person, person, pageable);
        }
        if (from.isAfter(till)) {
            String msg = DATES_SEQUENCE_ERROR + ": from: " + from + ", till: " + till;
            LOGGER.error("ServiceException: {}", msg);
            throw new ServiceException(msg);
        }
        return repository.findAllByInAccountPersonOrOutAccountPersonAndDateBetween(person, person, from, till, pageable);
    }

    @Override
    public Optional<Operation> getByIdAndPerson(Long id, Person person) {
        return repository.getByIdAndInAccountPersonOrOutAccountPerson(id, person, person);
    }

    @Override
    public long countOperationsByCategory(Category category) {
        return repository.countOperationsByCategory(category);
    }

    @Override
    public long countOperationsByAccounts(Account account) {
        return repository.countOperationsByInAccountOrOutAccount(account, account);
    }

    @Override
    public List<Object[]> findAllDailyVolumes(LocalDate fromDate, LocalDate tillDate, Person person) {
        return repository.findAllDailyVolumes(fromDate, tillDate, person);
    }

    @Override
    public <S extends Operation> S save(S entity) throws ServiceException {
        Account in = entity.getInAccount();
        Account out = entity.getOutAccount();
        Category category = entity.getCategory();
        checkLogicalConstraints(in, out, category);
        BigDecimal sum = entity.getSum();
        if (category == null) {
            in.setBalance(in.getBalance().add(sum));
            out.setBalance(out.getBalance().subtract(sum));
            return super.save(entity);
        }
        if (category.getType() == CategoryType.INCOME) {
            in.setBalance(in.getBalance().add(sum));
        }
        if (category.getType() == CategoryType.OUTCOME) {
            out.setBalance(out.getBalance().subtract(sum));
        }
        return super.save(entity);
    }

    private void checkLogicalConstraints(Account in, Account out, Category cat) throws ServiceException {
        if (in == null & out == null) {
            LOGGER.error("ServiceException: {}", NO_ACCOUNTS_ERROR);
            throw new ServiceException(NO_ACCOUNTS_ERROR);
        }
        if (cat == null & (in == null || out == null)) {
            LOGGER.error("ServiceException: {}", NO_ACCOUNTS_AND_CATEGORY_ERROR);
            throw new ServiceException(NO_ACCOUNTS_AND_CATEGORY_ERROR);
        }
        if (cat == null) {
            return;
        }
        if (in != null && out != null) {
            LOGGER.error("ServiceException: {}", NO_OPERATION_TYPE_ERROR);
            throw new ServiceException(NO_OPERATION_TYPE_ERROR);
        }
        CategoryType categoryType = cat.getType();
        if ((in != null && categoryType != CategoryType.INCOME) || (out != null && categoryType != CategoryType.OUTCOME)) {
            String msg = WRONG_CATEGORY_TYPE_ERROR + categoryType + '.';
            LOGGER.error("ServiceException: {}", msg);
            throw new ServiceException(msg);
        }
    }
}
