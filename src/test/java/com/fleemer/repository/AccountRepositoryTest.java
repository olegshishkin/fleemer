package com.fleemer.repository;

import static com.fleemer.model.EntityCreator.createAccount;

import com.fleemer.FleemerApplication;
import com.fleemer.model.Account;
import com.fleemer.model.Person;
import com.fleemer.model.enums.AccountType;
import com.fleemer.model.enums.Currency;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@Transactional
@DatabaseSetup({AccountRepositoryTest.INIT_DB_PATH})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ContextConfiguration(classes = {FleemerApplication.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class})
@RunWith(SpringRunner.class)
public class AccountRepositoryTest {
    private static final String CLASSPATH = "classpath:";
    private static final String DATASETS_PATH = CLASSPATH + "dbunit/account/";
    static final String INIT_DB_PATH = CLASSPATH + "dbunit/db_init.xml";

    private List<Account> accounts;
    private List<Person> people;

    @Autowired
    private AccountRepository repository;

    @Before
    public void setUp() {
        RepositoryTestingPopulationClass populationClass = new RepositoryTestingPopulationClass();
        accounts = populationClass.getAccounts();
        people = populationClass.getPeople();
    }

    @Test
    public void existsById() {
        Assert.assertTrue(repository.existsById(1L));
        Assert.assertFalse(repository.existsById(11L));
    }

    @Test
    public void findById() {
        Optional<Account> optional = repository.findById(1L);
        Assert.assertTrue(optional.isPresent());
        RepositoryAssertions.assertEquals(accounts.get(0), optional.get());
    }

    @Test
    public void getOne() {
        RepositoryAssertions.assertEquals(accounts.get(1), repository.getOne(2L));
    }

    @Test
    public void findAllById() {
        List<Account> expected = List.of(accounts.get(0), accounts.get(2));
        List<Account> actual = repository.findAllById(List.of(56L, 1L, 3L, 30L));
        RepositoryAssertions.assertIterableEquals(expected, actual);
    }

    @Test
    public void findAll() {
        List<Account> expected = List.of(accounts.get(0), accounts.get(1), accounts.get(2));
        RepositoryAssertions.assertIterableEquals(expected, repository.findAll());
    }

    @Test
    public void findAll_sort() {
        List<Account> expected = List.of(accounts.get(2), accounts.get(0), accounts.get(1));
        List<Account> actual = repository.findAll(new Sort(Sort.Direction.ASC, "person_id"));
        RepositoryAssertions.assertIterableEquals(expected, actual);
    }

    @Test
    public void count() {
        Assert.assertEquals(3L, repository.count());
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "save_new.xml")
    public void save_new() {
        BigDecimal sum = new BigDecimal(111.89);
        Account account = createAccount(null, AccountType.BANK_ACCOUNT, Currency.EUR, "Bank!", sum, people.get(4));
        repository.save(account);
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "save_existing.xml")
    public void save_existing() {
        Account account = accounts.get(0);
        account.setName("Save existing");
        repository.save(account);
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "save_all.xml")
    public void saveAll() {
        Account account1 = createAccount(null, AccountType.DEPOSIT, Currency.RUB, "Depo", new BigDecimal(0.0), people.get(1));
        Account account2 = createAccount(null, AccountType.CASH, Currency.USD, "My cash", new BigDecimal(0.124), people.get(2));
        Account account = accounts.get(0);
        account.setName("Save all");
        repository.saveAll(List.of(account1, account2, account));
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "delete_by_id.xml")
    public void deleteById() {
        repository.deleteById(2L);
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "delete.xml")
    public void delete() {
        repository.delete(accounts.get(1));
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "delete_in_batch.xml")
    public void deleteInBatch() {
        repository.deleteInBatch(List.of(accounts.get(1), accounts.get(0)));
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "delete_all_iterable.xml")
    public void deleteAll_iterable() {
        repository.deleteAll(List.of(accounts.get(1), accounts.get(0)));
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "delete_all.xml")
    public void deleteAll() {
        repository.deleteAll();
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "delete_all_in_batch.xml")
    public void deleteAllInBatch() {
        repository.deleteAllInBatch();
        repository.flush();
    }
}