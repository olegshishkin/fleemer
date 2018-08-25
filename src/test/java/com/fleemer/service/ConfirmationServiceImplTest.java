package com.fleemer.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import com.fleemer.model.Confirmation;
import com.fleemer.model.Person;
import com.fleemer.repository.ConfirmationRepository;
import com.fleemer.service.exception.ServiceException;
import com.fleemer.service.implementation.ConfirmationServiceImpl;
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
public class ConfirmationServiceImplTest {
    private long id = 123L;

    @InjectMocks
    private ConfirmationServiceImpl service;

    @Mock
    private ConfirmationRepository repository;

    @Mock
    private Confirmation confirmation;

    @Mock
    private Pageable pageable;

    @Mock
    private Person person;

    @Mock
    private Page<Confirmation> page;

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
        when(repository.getOne(id)).thenReturn(confirmation);
        assertEquals(confirmation, service.getOne(id));
        verify(repository, times(1)).getOne(id);
    }

    @Test
    public void findById() {
        Optional<Confirmation> optional = Optional.of(confirmation);
        when(repository.findById(id)).thenReturn(optional);
        assertEquals(optional, service.findById(id));
        verify(repository, times(1)).findById(id);
    }

    @Test
    public void findAllById() {
        List<Long> ids = Collections.emptyList();
        List<Confirmation> confirmations = Collections.emptyList();
        when(repository.findAllById(ids)).thenReturn(confirmations);
        assertEquals(confirmations, service.findAllById(ids));
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
        List<Confirmation> confirmations = Collections.emptyList();
        when(repository.findAll()).thenReturn(confirmations);
        assertEquals(confirmations, service.findAll());
        verify(repository, times(1)).findAll();
    }

    @Test
    public void findAll_sort() {
        List<Confirmation> confirmations = Collections.emptyList();
        when(repository.findAll(sort)).thenReturn(confirmations);
        assertEquals(confirmations, service.findAll(sort));
        verify(repository, times(1)).findAll(sort);
    }

    @Test
    public void save() throws ServiceException {
        when(repository.save(confirmation)).thenReturn(confirmation);
        assertEquals(confirmation, service.save(confirmation));
        verify(repository, times(1)).save(confirmation);
    }

    @Test
    public void saveAll() {
        List<Confirmation> confirmations = Collections.emptyList();
        when(repository.saveAll(confirmations)).thenReturn(confirmations);
        assertEquals(confirmations, service.saveAll(confirmations));
        verify(repository, times(1)).saveAll(confirmations);
    }

    @Test
    public void deleteById() {
        doNothing().when(repository).deleteById(id);
        service.deleteById(id);
        verify(repository, times(1)).deleteById(id);
    }

    @Test
    public void delete() {
        doNothing().when(repository).delete(confirmation);
        service.delete(confirmation);
        verify(repository, times(1)).delete(confirmation);
    }

    @Test
    public void deleteAll_iterable() {
        List<Confirmation> confirmations = Collections.emptyList();
        doNothing().when(repository).deleteAll(confirmations);
        service.deleteAll(confirmations);
        verify(repository, times(1)).deleteAll(confirmations);
    }

    @Test
    public void deleteAll() {
        doNothing().when(repository).deleteAll();
        service.deleteAll();
        verify(repository, times(1)).deleteAll();
    }

    @Test
    public void deleteInBatch() {
        List<Confirmation> confirmations = Collections.emptyList();
        doNothing().when(repository).deleteInBatch(confirmations);
        service.deleteInBatch(confirmations);
        verify(repository, times(1)).deleteInBatch(confirmations);
    }

    @Test
    public void deleteAllInBatch() {
        doNothing().when(repository).deleteAllInBatch();
        service.deleteAllInBatch();
        verify(repository, times(1)).deleteAllInBatch();
    }

    @Test
    public void isPersonEnabled() {
        Optional<Confirmation> expected = Optional.of(confirmation);
        when(repository.findByPersonAndEnabledIsTrue(person)).thenReturn(expected);
        assertTrue(service.isPersonEnabled(person));
        verify(repository, times(1)).findByPersonAndEnabledIsTrue(person);
    }

    @Test
    public void findByPersonEmail() {
        String email = "email";
        Optional<Confirmation> expected = Optional.of(confirmation);
        when(repository.findByPersonEmail(email)).thenReturn(expected);
        assertEquals(expected, service.findByPersonEmail(email));
        verify(repository, times(1)).findByPersonEmail(email);
    }
}