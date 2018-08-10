package com.fleemer.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import com.fleemer.model.Operation;
import com.fleemer.repository.OperationRepository;
import com.fleemer.service.implementation.OperationServiceImpl;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RunWith(MockitoJUnitRunner.class)
public class OperationServiceImplTest {
    private long id = 123L;

    @InjectMocks
    private OperationServiceImpl service;

    @Mock
    private OperationRepository repository;

    @Mock
    private Operation operation;

    @Mock
    private Pageable pageable;

    @Mock
    private Page<Operation> page;

    @Mock
    private Sort sort;

    @Test
    public void count() {
        when(repository.count()).thenReturn(id);
        assertEquals(id, service.count());
        verify(repository, times(1)).count();
    }

    @Test
    public void existsById() {
        when(repository.existsById(id)).thenReturn(true);
        assertTrue(service.existsById(id));
        verify(repository, times(1)).existsById(id);
    }

    @Test
    public void getOne() {
        when(repository.getOne(id)).thenReturn(operation);
        assertEquals(operation, service.getOne(id));
        verify(repository, times(1)).getOne(id);
    }

    @Test
    public void findById() {
        Optional<Operation> optional = Optional.of(operation);
        when(repository.findById(id)).thenReturn(optional);
        assertEquals(optional, service.findById(id));
        verify(repository, times(1)).findById(id);
    }

    @Test
    public void findAllById() {
        List<Long> ids = Collections.emptyList();
        List<Operation> operations = Collections.emptyList();
        when(repository.findAllById(ids)).thenReturn(operations);
        assertEquals(operations, service.findAllById(ids));
        verify(repository, times(1)).findAllById(ids);
    }

    @Test
    public void findAll_pageable() {
        when(repository.findAll(pageable)).thenReturn(page);
        assertEquals(page, service.findAll(pageable));
        verify(repository, times(1)).findAll(pageable);
    }

    @Test
    public void findAll() {
        List<Operation> operations = Collections.emptyList();
        when(repository.findAll()).thenReturn(operations);
        assertEquals(operations, service.findAll());
        verify(repository, times(1)).findAll();
    }

    @Test
    public void findAll_sort() {
        List<Operation> operations = Collections.emptyList();
        when(repository.findAll(sort)).thenReturn(operations);
        assertEquals(operations, service.findAll(sort));
        verify(repository, times(1)).findAll(sort);
    }

    @Test
    public void saveAll() {
        List<Operation> operations = Collections.emptyList();
        when(repository.saveAll(operations)).thenReturn(operations);
        assertEquals(operations, service.saveAll(operations));
        verify(repository, times(1)).saveAll(operations);
    }

    @Test
    public void deleteById() {
        doNothing().when(repository).deleteById(id);
        service.deleteById(id);
        verify(repository, times(1)).deleteById(id);
    }

    @Test
    public void delete() {
        doNothing().when(repository).delete(operation);
        service.delete(operation);
        verify(repository, times(1)).delete(operation);
    }

    @Test
    public void deleteAll_iterable() {
        List<Operation> operations = Collections.emptyList();
        doNothing().when(repository).deleteAll(operations);
        service.deleteAll(operations);
        verify(repository, times(1)).deleteAll(operations);
    }

    @Test
    public void deleteAll() {
        doNothing().when(repository).deleteAll();
        service.deleteAll();
        verify(repository, times(1)).deleteAll();
    }

    @Test
    public void deleteInBatch() {
        List<Operation> operations = Collections.emptyList();
        doNothing().when(repository).deleteInBatch(operations);
        service.deleteInBatch(operations);
        verify(repository, times(1)).deleteInBatch(operations);
    }

    @Test
    public void deleteAllInBatch() {
        doNothing().when(repository).deleteAllInBatch();
        service.deleteAllInBatch();
        verify(repository, times(1)).deleteAllInBatch();
    }
}