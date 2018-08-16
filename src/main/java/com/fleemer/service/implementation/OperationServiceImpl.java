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
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OperationServiceImpl extends AbstractService<Operation, Long, OperationRepository>
        implements OperationService {
    private static final String NO_ACCOUNTS_AND_CATEGORY_ERR_KEY = "index.error.no-accounts-and-category-chosen";
    private static final String NO_ACCOUNTS_ERROR_KEY = "index.error.no-accounts-chosen";
    private static final String NO_OPERATION_TYPE_ERR_KEY = "index.error.no-way-operation-determine";
    private static final String WRONG_CATEGORY_TYPE_ERR_KEY = "index.error.wrong-category-type-chosen";

    private final OperationRepository repository;
    private final MessageSource messageSource;

    @Autowired
    public OperationServiceImpl(OperationRepository repository, MessageSource messageSource) {
        this.repository = repository;
        this.messageSource = messageSource;
    }

    @Override
    protected OperationRepository getRepository() {
        return repository;
    }

    @Override
    public List<Operation> findAllByPerson(Person person) {
        return repository.findAllByInAccountPersonOrOutAccountPerson(person, person);
    }

    @Override
    public Page<Operation> findAllByPerson(Person person, Pageable pageable) {
        return repository.findAllByInAccountPersonOrOutAccountPerson(person, person, pageable);
    }

    @Override
    public List<Operation> findAllByCategory(Category category) {
        return repository.findAllByCategory(category);
    }

    @Override
    public List<Operation> findAllByAccount(Account account) {
        return repository.findAllByInAccountOrOutAccount(account, account);
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
            String message = messageSource.getMessage(NO_ACCOUNTS_ERROR_KEY, null, Locale.getDefault());
            throw new ServiceException(message);
        }
        if (cat == null & (in == null || out == null)) {
            String message = messageSource.getMessage(NO_ACCOUNTS_AND_CATEGORY_ERR_KEY, null, Locale.getDefault());
            throw new ServiceException(message);
        }
        if (cat == null) {
            return;
        }
        if (in != null && out != null) {
            String message = messageSource.getMessage(NO_OPERATION_TYPE_ERR_KEY, null, Locale.getDefault());
            throw new ServiceException(message);
        }
        CategoryType categoryType = cat.getType();
        if ((in != null && categoryType != CategoryType.INCOME) || (out != null && categoryType != CategoryType.OUTCOME)) {
            Object[] args = new Object[]{categoryType};
            String message = messageSource.getMessage(WRONG_CATEGORY_TYPE_ERR_KEY, args, Locale.getDefault());
            throw new ServiceException(message);
        }
    }
}
