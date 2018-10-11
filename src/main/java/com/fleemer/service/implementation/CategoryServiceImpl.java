package com.fleemer.service.implementation;

import com.fleemer.model.Category;
import com.fleemer.model.Person;
import com.fleemer.model.enums.CategoryType;
import com.fleemer.repository.CategoryRepository;
import com.fleemer.service.CategoryService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryServiceImpl extends AbstractService<Category, Long, CategoryRepository>
        implements CategoryService {
    private final CategoryRepository repository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    protected CategoryRepository getRepository() {
        return repository;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Category> findAll(Person person) {
        return repository.findAllByPersonOrderByName(person);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Optional<Category> findByNameAndPerson(String name, Person person) {
        return repository.findByNameAndPerson(name, person);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Category> findAllByTypeAndPerson(CategoryType type, Person person) {
        return repository.findAllByTypeAndPerson(type, person);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Optional<Category> findByIdAndPerson(Long id, Person person) {
        return repository.findByIdAndPerson(id, person);
    }
}
