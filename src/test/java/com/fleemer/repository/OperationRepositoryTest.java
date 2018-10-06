package com.fleemer.repository;

import static com.fleemer.model.EntityCreator.create;
import static java.sql.Date.valueOf;

import com.fleemer.FleemerApplication;
import com.fleemer.model.Account;
import com.fleemer.model.Category;
import com.fleemer.model.Operation;
import com.fleemer.model.Person;
import com.fleemer.model.enums.Currency;
import com.fleemer.service.specification.OperationSpecification;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.*;
import java.util.*;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@Transactional
@DatabaseSetup({OperationRepositoryTest.INIT_DB_PATH})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ContextConfiguration(classes = {FleemerApplication.class, TestConfigForMail.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class})
@RunWith(SpringRunner.class)
public class OperationRepositoryTest {
    private static final String CLASSPATH = "classpath:";
    private static final String DATASETS_PATH = CLASSPATH + "dbunit/operation/";
    static final String INIT_DB_PATH = CLASSPATH + "dbunit/db_init.xml";

    private List<Account> accounts;
    private List<Category> categories;
    private List<Operation> operations;
    private List<Person> people;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private OperationRepository repository;

    @Before
    public void setUp() {
        RepositoryTestingPopulationClass populationClass = new RepositoryTestingPopulationClass();
        accounts = populationClass.getAccounts();
        categories = populationClass.getCategories();
        operations = populationClass.getOperations();
        people = populationClass.getPeople();
    }

    @Test
    public void existsById() {
        Assert.assertTrue(repository.existsById(1L));
        Assert.assertFalse(repository.existsById(11L));
    }

    @Test
    public void findById() {
        Optional<Operation> optional = repository.findById(1L);
        Assert.assertTrue(optional.isPresent());
        RepositoryAssertions.assertEquals(operations.get(0), optional.get());
    }

    @Test
    public void getOne() {
        RepositoryAssertions.assertEquals(operations.get(1), repository.getOne(2L));
    }

    @Test
    public void findAllById() {
        List<Operation> expected = List.of(operations.get(0), operations.get(1), operations.get(3), operations.get(7));
        List<Operation> actual = repository.findAllById(List.of(56L, 1L, 2L, 58L, 30L, 83L, 8L, 654L, 4L));
        RepositoryAssertions.assertIterableEquals(expected, actual);
    }

    @Test
    public void findAll() {
        RepositoryAssertions.assertIterableEquals(operations, repository.findAll());
    }

    @Test
    public void findAll_sort() {
        List<Operation> expected = new ArrayList<>();
        expected.add(operations.get(2));
        expected.add(operations.get(0));
        expected.add(operations.get(7));
        expected.add(operations.get(4));
        expected.add(operations.get(1));
        expected.add(operations.get(6));
        expected.add(operations.get(3));
        expected.add(operations.get(5));
        expected.add(operations.get(8));
        List<Operation> actual = repository.findAll(new Sort(Sort.Direction.DESC, "sum"));
        RepositoryAssertions.assertIterableEquals(expected, actual);
    }

    @Test
    public void count() {
        Assert.assertEquals(9L, repository.count());
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "save_new.xml")
    public void save_new() {
        Category category = categories.get(6);
        category.setId(null);
        entityManager.persist(category);
        Account inAccount = accounts.get(1);
        inAccount.setId(null);
        entityManager.persist(inAccount);
        repository.save(create(null, LocalDate.of(2000, Month.FEBRUARY, 4), inAccount, null, category, 7.98,
                "new comment", 0));
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "save_existing.xml")
    public void save_existing() {
        Operation operation = operations.get(3);
        operation.setComment("Changed comment");
        repository.save(operation);
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "save_all.xml")
    public void saveAll() {
        Account inAccount1 = accounts.get(1);
        inAccount1.setId(null);
        entityManager.persist(inAccount1);
        Account outAccount1 = accounts.get(2);
        outAccount1.setId(null);
        entityManager.persist(outAccount1);
        Operation o1 = create(null, LocalDate.of(2018, Month.MAY, 14), inAccount1, outAccount1, null, 7.98,
                "new comment1", 0);
        Account outAccount2 = accounts.get(0);
        outAccount2.setId(null);
        entityManager.persist(outAccount2);
        Category category = categories.get(1);
        category.setId(null);
        entityManager.persist(category);
        Operation o2 = create(null, LocalDate.of(2018, Month.MAY, 14), null, outAccount2, category, -17.98,
                "new comment2", 0);
        Operation o = operations.get(3);
        o.setComment("Changed comment");
        repository.saveAll(List.of(o1, o2, o));
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "delete_by_id.xml")
    public void deleteById() {
        repository.deleteById(3L);
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "delete.xml")
    public void delete() {
        repository.delete(operations.get(3));
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "delete_in_batch.xml")
    public void deleteInBatch() {
        repository.deleteInBatch(List.of(operations.get(3), operations.get(1)));
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "delete_all_iterable.xml")
    public void deleteAll_iterable() {
        repository.deleteAll(List.of(operations.get(2), operations.get(8)));
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

    @Test
    public void findAllByInAccountPersonOrOutAccountPerson() {
        Person person = people.get(1);
        List<Operation> expected = List.of(operations.get(1),
                operations.get(2),
                operations.get(3),
                operations.get(4),
                operations.get(6),
                operations.get(7),
                operations.get(8));
        List<Operation> actual = repository.findAllByInAccountPersonOrOutAccountPerson(person, person);
        RepositoryAssertions.assertIterableEquals(expected, actual);
    }

    @Test
    public void findAllByInAccountPersonOrOutAccountPersonAndDateBetween() {
        Person person = people.get(3);
        LocalDate from = LocalDate.of(2018, 1, 1);
        LocalDate till = LocalDate.of(2018, 12, 31);
        List<Operation> expected = List.of(operations.get(0), operations.get(4));
        List<Operation> actual = repository.findAllByInAccountPersonOrOutAccountPersonAndDateBetween(person, person,
                from, till);
        RepositoryAssertions.assertIterableEquals(expected, actual);
    }

    @Test
    public void getByIdAndInAccountPersonOrOutAccountPerson() {
        Person person = people.get(1);
        Operation actual = repository.getByIdAndInAccountPersonOrOutAccountPerson(4L, person, person).orElseThrow();
        RepositoryAssertions.assertEquals(operations.get(3), actual);
    }

    @Test
    public void countOperationsByCategory() {
        Assert.assertEquals(2L, repository.countOperationsByCategory(categories.get(6)));
    }

    @Test
    public void countOperationsByInAccountOrOutAccount() {
        Account account = accounts.get(1);
        Assert.assertEquals(4L, repository.countOperationsByInAccountOrOutAccount(account, account));
    }

    @Test
    public void findAllDailyVolumes() {
        Person person = people.get(3);
        LocalDate from = LocalDate.of(2018, 1, 1);
        LocalDate till = LocalDate.of(2018, 12, 31);
        BigDecimal volume = new BigDecimal("00000000000000004567.9800000000");
        Object[] object = new Object[]{valueOf(LocalDate.of(2018, 5, 3)), volume};
        List<Object[]> expected = new ArrayList<>();
        expected.add(object);
        List<Object[]> actual = repository.findAllDailyVolumes(Currency.USD.ordinal(), from, till, person);
        assertIterableEquals(expected, actual);
    }

    @Test
    public void findAll_bySpecification() {
        Person person = people.get(3);
        LocalDate from = LocalDate.of(2017, 5, 12);
        LocalDate till = LocalDate.of(2018, 6, 30);
        Account inAccount = accounts.get(1);
        BigDecimal max = new BigDecimal("00000000000000000500.0000000000");
        boolean orMode = false;
        String commentPattern = "6";
        Pageable pageable = PageRequest.of(0, 10, new Sort(Sort.Direction.ASC, "date"));
        Specification<Operation> specification = OperationSpecification.createSpecification(orMode, person, from, till,
                List.of(inAccount), null, null, null, max, commentPattern);
        List<Operation> expected = List.of(operations.get(5));
        RepositoryAssertions.assertIterableEquals(expected, repository.findAll(specification, pageable).getContent());
    }

    public static void assertIterableEquals(List<Object[]> expected, List<Object[]> actual) {
        if (expected == actual) {
            return;
        }
        Assert.assertTrue(expected != null & actual != null);
        Iterator<Object[]> expectedIterator = expected.iterator();
        Iterator<Object[]> actualIterator = actual.iterator();
        while (expectedIterator.hasNext() && actualIterator.hasNext()) {
            Object[] expectedElement = expectedIterator.next();
            Object[] actualElement = actualIterator.next();
            if (expectedElement == actualElement) {
                continue;
            }
            if (expectedElement[0] != null) {
                Date actualDate = (Date) actualElement[0];
                Assert.assertEquals(expectedElement[0], actualDate);
            }
            if (expectedElement[1] != null) {
                Assert.assertEquals(expectedElement[1], actualElement[1]);
            }
            if (expectedElement.length > 2) {
                Assert.assertEquals(expectedElement[2], actualElement[2]);
            }
        }
        Assert.assertFalse(expectedIterator.hasNext());
        Assert.assertFalse(actualIterator.hasNext());
    }
}