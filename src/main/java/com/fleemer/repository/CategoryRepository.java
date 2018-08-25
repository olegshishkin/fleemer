package com.fleemer.repository;

import com.fleemer.model.Category;
import com.fleemer.model.Person;
import com.fleemer.model.enums.CategoryType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByPersonOrderByName(Person person);

    Optional<Category> findByNameAndPerson(String name, Person person);

    List<Category> findAllByTypeAndPerson(CategoryType type, Person person);

    Optional<Category> findByIdAndPerson(Long id, Person person);
}
