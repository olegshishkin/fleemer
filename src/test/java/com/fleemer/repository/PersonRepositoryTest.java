package com.fleemer.repository;

import static com.fleemer.model.EntityCreator.create;

import com.fleemer.FleemerApplication;
import com.fleemer.model.Person;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@Transactional
@DatabaseSetup({PersonRepositoryTest.INIT_DB_PATH})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ContextConfiguration(classes = {FleemerApplication.class, TestConfigForMail.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class})
@RunWith(SpringRunner.class)
public class PersonRepositoryTest {
    private static final String CLASSPATH = "classpath:";
    private static final String DATASETS_PATH = CLASSPATH + "dbunit/person/";
    static final String INIT_DB_PATH = CLASSPATH + "dbunit/db_init.xml";

    private List<Person> people;

    @Autowired
    private PersonRepository repository;

    @Before
    public void setUp() {
        people = new RepositoryTestingPopulationClass().getPeople();
    }

    @Test
    public void existsById() {
        Assert.assertTrue(repository.existsById(1L));
        Assert.assertFalse(repository.existsById(11L));
    }

    @Test
    public void findById() {
        Optional<Person> optional = repository.findById(1L);
        Assert.assertTrue(optional.isPresent());
        RepositoryAssertions.assertEquals(people.get(0), optional.get());
    }

    @Test
    public void getOne() {
        RepositoryAssertions.assertEquals(people.get(1), repository.getOne(2L));
    }

    @Test
    public void findAllById() {
        List<Person> expected = List.of(people.get(0), people.get(2), people.get(3));
        List<Person> actual = repository.findAllById(List.of(56L, 1L, 4L, 30L, 3L));
        RepositoryAssertions.assertIterableEquals(expected, actual);
    }

    @Test
    public void findAll() {
        RepositoryAssertions.assertIterableEquals(people, repository.findAll());
    }

    @Test
    public void findAll_sort() {
        List<Person> expected = List.of(people.get(0), people.get(2), people.get(3), people.get(1), people.get(4));
        List<Person> actual = repository.findAll(new Sort(Sort.Direction.ASC, "email"));
        RepositoryAssertions.assertIterableEquals(expected, actual);
    }

    @Test
    public void count() {
        Assert.assertEquals(5L, repository.count());
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "save_new.xml",
            assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED)
    public void save_new() {
        String firstName = "test firstname";
        Person person = create(null, firstName, "test lastname", "test nickname", "test@email.com", "test hash", 0);
        repository.save(person);
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "save_existing.xml",
            assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED)
    public void save_existing() {
        Person person = people.get(1);
        person.setEmail("Changed@email.ch");
        repository.save(person);
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "save_all.xml",
            assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED)
    public void saveAll() {
        String firstName1 = "New Firstname1";
        Person newPerson1 = create(null, firstName1, "New Lastname1", "New Nickname1", "New@email1.co", "New hash1", 0);
        String firstName2 = "New Firstname2";
        Person newPerson2 = create(null, firstName2, "New Lastname2", "New Nickname2", "New@email2.ui", "New hash2", 0);
        Person person = people.get(1);
        person.setEmail("Changed@email.ill");
        repository.saveAll(List.of(newPerson1, newPerson2, person));
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "delete_by_id.xml",
            assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED)
    public void deleteById() {
        repository.deleteById(1L);
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "delete.xml", assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED)
    public void delete() {
        repository.delete(people.get(3));
        repository.flush();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void deleteInBatch() {
        String message = "Batch deletion not supported for Person entity.";
        try {
            repository.deleteInBatch(List.of(people.get(2), people.get(0)));
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals(message, e.getMessage());
            throw e;
        }
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "delete_all_iterable.xml",
            assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED)
    public void deleteAll_iterable() {
        repository.deleteAll(List.of(people.get(3), people.get(4)));
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "delete_all.xml",
            assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED)
    public void deleteAll() {
        repository.deleteAll();
        repository.flush();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void deleteAllInBatch() {
        String message = "Batch deletion not supported for Person entity.";
        try {
            repository.deleteAllInBatch();
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals(message, e.getMessage());
            throw e;
        }
    }

    @Test
    public void findByEmail() {
        RepositoryAssertions.assertEquals(people.get(2), repository.findByEmail("mail3@mail.ma").orElseThrow());
        Assert.assertEquals(Optional.empty(), repository.findByEmail("Mail3@mail.ma"));
    }

    @Test
    public void findAllByNicknameContainsIgnoreCase() {
        List<Person> expected = List.of(people.get(1), people.get(2));
        Pageable pageable = PageRequest.of(0, 1000, new Sort(Sort.Direction.ASC, "id"));
        Page<Person> actual = repository.findAllByNicknameContainsIgnoreCase("iC", pageable);
        RepositoryAssertions.assertIterableEquals(expected, actual);
    }

    @Test
    public void findByNickname() {
        RepositoryAssertions.assertEquals(people.get(3), repository.findByNickname("Samuel").orElseThrow());
        Assert.assertEquals(Optional.empty(), repository.findByNickname("samuel"));
    }
}