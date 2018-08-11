package com.fleemer.service;

import com.fleemer.model.Category;
import com.fleemer.model.Person;
import com.fleemer.model.enums.CategoryType;
import java.util.List;
import java.util.Optional;

public interface CategoryService extends BaseService<Category, Long> {
    List<Category> findAll(Person person);

    Optional<Category> findByNameAndPerson(String name, Person person);

    List<Category> findAllByTypeAndPerson(CategoryType type, Person person);
}
