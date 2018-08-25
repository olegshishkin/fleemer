package com.fleemer.repository;

import static com.fleemer.model.EntityCreator.create;

import com.fleemer.FleemerApplication;
import com.fleemer.model.Confirmation;
import com.fleemer.model.Person;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import java.util.*;
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
@DatabaseSetup({ConfirmationRepositoryTest.INIT_DB_PATH})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ContextConfiguration(classes = {FleemerApplication.class, TestConfigForMail.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class})
@RunWith(SpringRunner.class)
public class ConfirmationRepositoryTest {
    private static final String CLASSPATH = "classpath:";
    private static final String DATASETS_PATH = CLASSPATH + "dbunit/confirmation/";
    static final String INIT_DB_PATH = CLASSPATH + "dbunit/db_init.xml";

    private List<Person> people;
    private List<Confirmation> confirmations;

    @Autowired
    private ConfirmationRepository repository;

    @Before
    public void setUp() {
        RepositoryTestingPopulationClass populationClass = new RepositoryTestingPopulationClass();
        people = populationClass.getPeople();
        confirmations = populationClass.getConfirmations();
    }

    @Test
    public void existsById() {
        Assert.assertTrue(repository.existsById(1L));
        Assert.assertFalse(repository.existsById(11L));
    }

    @Test
    public void findById() {
        Optional<Confirmation> optional = repository.findById(1L);
        Assert.assertTrue(optional.isPresent());
        RepositoryAssertions.assertEquals(confirmations.get(0), optional.get());
    }

    @Test
    public void getOne() {
        RepositoryAssertions.assertEquals(confirmations.get(1), repository.getOne(2L));
    }

    @Test
    public void findAllById() {
        List<Confirmation> expected = List.of(confirmations.get(0), confirmations.get(2));
        List<Confirmation> actual = repository.findAllById(List.of(1L, 4L, 30L, 3L));
        RepositoryAssertions.assertIterableEquals(expected, actual);
    }

    @Test
    public void findAll() {
        RepositoryAssertions.assertIterableEquals(confirmations, repository.findAll());
    }

    @Test
    public void findAll_sort() {
        List<Confirmation> expected = List.of(confirmations.get(2), confirmations.get(1), confirmations.get(0));
        List<Confirmation> actual = repository.findAll(new Sort(Sort.Direction.DESC, "token"));
        RepositoryAssertions.assertIterableEquals(expected, actual);
    }

    @Test
    public void count() {
        Assert.assertEquals(3L, repository.count());
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "save_new.xml")
    public void save_new() {
        repository.save(create(null, "11111111-1111-1111-1111-111111111111", false, people.get(2), 0));
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "save_existing.xml")
    public void save_existing() {
        Confirmation confirmation = confirmations.get(2);
        confirmation.setToken("11111111-1111-1111-1111-111111111111");
        repository.save(confirmation);
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "save_all.xml")
    public void saveAll() {
        Confirmation newConfirmation1 = create(null, "11111111-1111-1111-1111-111111111111", true, people.get(3), 0);
        Confirmation newConfirmation2 = create(null, "22222222-2222-2222-2222-222222222222", false, people.get(1), 0);
        Confirmation confirmation = confirmations.get(1);
        confirmation.setToken("33333333-3333-3333-3333-333333333333");
        repository.saveAll(List.of(newConfirmation1, newConfirmation2, confirmation));
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
        repository.delete(confirmations.get(0));
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "delete_in_batch.xml")
    public void deleteInBatch() {
        repository.deleteInBatch(List.of(confirmations.get(1), confirmations.get(2)));
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "delete_all_iterable.xml")
    public void deleteAll_iterable() {
        repository.deleteAll(List.of(confirmations.get(0), confirmations.get(1)));
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
    public void findByPersonAndEnabledIsTrue() {
        Confirmation expected = confirmations.get(1);
        Confirmation actual = repository.findByPersonAndEnabledIsTrue(people.get(1)).orElseThrow();
        RepositoryAssertions.assertEquals(expected, actual);
    }

    @Test
    public void findByPersonEmail() {
        Confirmation actual = repository.findByPersonEmail("mail95@mail.ma").orElseThrow();
        RepositoryAssertions.assertEquals(confirmations.get(1), actual);
    }
}