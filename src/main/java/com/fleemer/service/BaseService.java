package com.fleemer.service;

import com.fleemer.service.exception.ServiceException;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface BaseService<T, ID> {
    long count();

    boolean existsById(ID id);

    T getOne(ID id);

    Optional<T> findById(ID id);

    Iterable<T> findAllById(Iterable<ID> ids);

    Page<T> findAll(Pageable pageable);

    Iterable<T> findAll();

    Iterable<T> findAll(Sort sort);

    <S extends T> S save(S entity) throws ServiceException;

    <S extends T> Iterable<S> saveAll(Iterable<S> entities);

    void deleteById(ID id);

    void delete(T entity) throws ServiceException;

    void deleteAll(Iterable<? extends T> entities);

    void deleteAll();

    void deleteInBatch(Iterable<T> entities);

    void deleteAllInBatch();
}
