package com.fleemer.service.implementation;

import com.fleemer.service.BaseService;
import java.util.Optional;
import org.hibernate.service.spi.ServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class AbstractService <T, ID, R extends JpaRepository<T, ID>> implements BaseService<T, ID> {
    @Override
    public long count() {
        return getRepository().count();
    }

    @Override
    public boolean existsById(ID id) {
        return getRepository().existsById(id);
    }

    @Override
    public T getOne(ID id) {
        return getRepository().getOne(id);
    }

    @Override
    public Optional<T> findById(ID id) {
        return getRepository().findById(id);
    }

    @Override
    public Iterable<T> findAllById(Iterable<ID> ids) {
        return getRepository().findAllById(ids);
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        return getRepository().findAll(pageable);
    }

    @Override
    public Iterable<T> findAll() {
        return getRepository().findAll();
    }

    @Override
    public Iterable<T> findAll(Sort sort) {
        return getRepository().findAll(sort);
    }

    @Override
    public <S extends T> S save(S entity) throws ServiceException, com.fleemer.service.exception.ServiceException {
        return getRepository().save(entity);
    }

    @Override
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
        return getRepository().saveAll(entities);
    }

    @Override
    public void deleteById(ID id) {
        getRepository().deleteById(id);
    }

    @Override
    public void delete(T entity) {
        getRepository().delete(entity);
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        getRepository().deleteAll(entities);
    }

    @Override
    public void deleteAll() {
        getRepository().deleteAll();
    }

    @Override
    public void deleteInBatch(Iterable<T> entities) {
        getRepository().deleteInBatch(entities);
    }

    @Override
    public void deleteAllInBatch() {
        getRepository().deleteAllInBatch();
    }

    protected abstract R getRepository();
}
