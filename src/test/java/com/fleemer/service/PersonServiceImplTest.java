package com.fleemer.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import com.fleemer.model.Person;
import com.fleemer.repository.PersonRepository;
import com.fleemer.service.exception.ServiceException;
import com.fleemer.service.implementation.PersonServiceImpl;
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
public class PersonServiceImplTest {
    private long id = 123L;

    @InjectMocks
    private PersonServiceImpl service;

    @Mock
    private PersonRepository repository;

    @Mock
    private Person person;

    @Mock
    private Pageable pageable;

    @Mock
    private Page<Person> page;

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
        when(repository.getOne(id)).thenReturn(person);
        assertEquals(person, service.getOne(id));
        verify(repository, times(1)).getOne(id);
    }

    @Test
    public void findById() {
        Optional<Person> optional = Optional.of(person);
        when(repository.findById(id)).thenReturn(optional);
        assertEquals(optional, service.findById(id));
        verify(repository, times(1)).findById(id);
    }

    @Test
    public void findAllById() {
        List<Long> ids = Collections.emptyList();
        List<Person> people = Collections.emptyList();
        when(repository.findAllById(ids)).thenReturn(people);
        assertEquals(people, service.findAllById(ids));
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
        List<Person> people = Collections.emptyList();
        when(repository.findAll()).thenReturn(people);
        assertEquals(people, service.findAll());
        verify(repository, times(1)).findAll();
    }

    @Test
    public void findAll_sort() {
        List<Person> people = Collections.emptyList();
        when(repository.findAll(sort)).thenReturn(people);
        assertEquals(people, service.findAll(sort));
        verify(repository, times(1)).findAll(sort);
    }

    @Test
    public void save() throws ServiceException {
        when(repository.save(person)).thenReturn(person);
        assertEquals(person, service.save(person));
        verify(repository, times(1)).save(person);
    }

    @Test
    public void saveAll() {
        List<Person> people = Collections.emptyList();
        when(repository.saveAll(people)).thenReturn(people);
        assertEquals(people, service.saveAll(people));
        verify(repository, times(1)).saveAll(people);
    }

    @Test
    public void deleteById() {
        doNothing().when(repository).deleteById(id);
        service.deleteById(id);
        verify(repository, times(1)).deleteById(id);
    }

    @Test
    public void delete() throws ServiceException {
        doNothing().when(repository).delete(person);
        service.delete(person);
        verify(repository, times(1)).delete(person);
    }

    @Test
    public void deleteAll_iterable() {
        List<Person> people = Collections.emptyList();
        doNothing().when(repository).deleteAll(people);
        service.deleteAll(people);
        verify(repository, times(1)).deleteAll(people);
    }

    @Test
    public void deleteAll() {
        doNothing().when(repository).deleteAll();
        service.deleteAll();
        verify(repository, times(1)).deleteAll();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void deleteInBatch() {
        List<Person> people = Collections.emptyList();
        doThrow(UnsupportedOperationException.class).when(repository).deleteInBatch(people);
        service.deleteInBatch(people);
        verify(repository, times(1)).deleteInBatch(people);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void deleteAllInBatch() {
        doThrow(UnsupportedOperationException.class).when(repository).deleteAllInBatch();
        service.deleteAllInBatch();
        verify(repository, times(1)).deleteAllInBatch();
    }

    @Test
    public void findByEmail() {
        Optional<Person> expected = Optional.of(person);
        when(repository.findByEmail("email")).thenReturn(expected);
        assertEquals(expected, service.findByEmail("email"));
        verify(repository, times(1)).findByEmail("email");
    }
}