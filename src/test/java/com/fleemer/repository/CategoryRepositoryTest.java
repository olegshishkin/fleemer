package com.fleemer.repository;

import static com.fleemer.model.EntityCreator.createCategory;

import com.fleemer.FleemerApplication;
import com.fleemer.model.Category;
import com.fleemer.model.Person;
import com.fleemer.model.enums.CategoryType;
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
@DatabaseSetup({CategoryRepositoryTest.INIT_DB_PATH})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ContextConfiguration(classes = {FleemerApplication.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class})
@RunWith(SpringRunner.class)
public class CategoryRepositoryTest {
    private static final String CLASSPATH = "classpath:";
    private static final String DATASETS_PATH = CLASSPATH + "dbunit/category/";
    static final String INIT_DB_PATH = CLASSPATH + "dbunit/db_init.xml";

    private List<Person> people;
    private List<Category> categories;

    @Autowired
    private CategoryRepository repository;

    @Before
    public void setUp() {
        RepositoryTestingPopulationClass populationClass = new RepositoryTestingPopulationClass();
        people = populationClass.getPeople();
        categories = populationClass.getCategories();
    }

    @Test
    public void existsById() {
        Assert.assertTrue(repository.existsById(1L));
        Assert.assertFalse(repository.existsById(11L));
    }

    @Test
    public void findById() {
        Optional<Category> optional = repository.findById(1L);
        Assert.assertTrue(optional.isPresent());
        RepositoryAssertions.assertEquals(categories.get(0), optional.get());
    }

    @Test
    public void getOne() {
        RepositoryAssertions.assertEquals(categories.get(1), repository.getOne(2L));
    }

    @Test
    public void findAllById() {
        List<Category> expected = List.of(categories.get(0), categories.get(2), categories.get(3));
        List<Category> actual = repository.findAllById(List.of(56L, 1L, 4L, 30L, 3L));
        RepositoryAssertions.assertIterableEquals(expected, actual);
    }

    @Test
    public void findAll() {
        RepositoryAssertions.assertIterableEquals(categories, repository.findAll());
    }

    @Test
    public void findAll_sort() {
        List<Category> expected = List.of(categories.get(0), categories.get(3), categories.get(4), categories.get(1),
                categories.get(5), categories.get(2), categories.get(6));
        List<Category> actual = repository.findAll(new Sort(Sort.Direction.ASC, "name"));
        RepositoryAssertions.assertIterableEquals(expected, actual);
    }

    @Test
    public void count() {
        Assert.assertEquals(7L, repository.count());
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "save_new.xml")
    public void save_new() {
        repository.save(createCategory(null, "new category", CategoryType.INCOME, people.get(2)));
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "save_existing.xml")
    public void save_existing() {
        Category category = categories.get(4);
        category.setName("Changed name");
        repository.save(category);
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "save_all.xml")
    public void saveAll() {
        Category newCategory1 = createCategory(null, "new category1", CategoryType.INCOME, people.get(3));
        Category newCategory2 = createCategory(null, "new category2", CategoryType.OUTCOME, people.get(1));
        Category category = categories.get(5);
        category.setName("Changed name");
        repository.saveAll(List.of(newCategory1, newCategory2, category));
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
        repository.delete(categories.get(3));
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "delete_in_batch.xml")
    public void deleteInBatch() {
        repository.deleteInBatch(List.of(categories.get(5), categories.get(2)));
        repository.flush();
    }

    @Test
    @ExpectedDatabase(value = DATASETS_PATH + "delete_all_iterable.xml")
    public void deleteAll_iterable() {
        repository.deleteAll(List.of(categories.get(3), categories.get(4)));
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
    public void findAllByPersonOrderByName() {
        List<Category> expected = List.of(categories.get(3), categories.get(5));
        List<Category> actual = repository.findAllByPersonOrderByName(people.get(1));
        RepositoryAssertions.assertIterableEquals(expected, actual);
    }

    @Test
    public void findByNameAndPerson() {
        Category actual = repository.findByNameAndPerson("Salary", people.get(3)).orElseThrow();
        RepositoryAssertions.assertEquals(categories.get(6), actual);
    }

    @Test
    public void findAllByTypeAndPerson() {
        Iterable<Category> actual = repository.findAllByTypeAndPerson(CategoryType.OUTCOME, people.get(1));
        RepositoryAssertions.assertIterableEquals(List.of(categories.get(3), categories.get(5)), actual);
    }
}