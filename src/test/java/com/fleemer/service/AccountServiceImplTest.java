package com.fleemer.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import com.fleemer.model.Account;
import com.fleemer.model.Person;
import com.fleemer.repository.AccountRepository;
import com.fleemer.service.exception.ServiceException;
import com.fleemer.service.implementation.AccountServiceImpl;
import java.math.BigDecimal;
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
public class AccountServiceImplTest {
    private long id = 123L;

    @InjectMocks
    private AccountServiceImpl service;

    @Mock
    private AccountRepository repository;

    @Mock
    private Account account;

    @Mock
    private Person person;

    @Mock
    private Pageable pageable;

    @Mock
    private Page<Account> page;

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
        when(repository.getOne(id)).thenReturn(account);
        assertEquals(account, service.getOne(id));
        verify(repository, times(1)).getOne(id);
    }

    @Test
    public void findById() {
        Optional<Account> optional = Optional.of(account);
        when(repository.findById(id)).thenReturn(optional);
        assertEquals(optional, service.findById(id));
        verify(repository, times(1)).findById(id);
    }

    @Test
    public void findAllById() {
        List<Long> ids = Collections.emptyList();
        List<Account> accounts = Collections.emptyList();
        when(repository.findAllById(ids)).thenReturn(accounts);
        assertEquals(accounts, service.findAllById(ids));
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
        List<Account> accounts = Collections.emptyList();
        when(repository.findAll()).thenReturn(accounts);
        assertEquals(accounts, service.findAll());
        verify(repository, times(1)).findAll();
    }

    @Test
    public void findAll_sort() {
        List<Account> accounts = Collections.emptyList();
        when(repository.findAll(sort)).thenReturn(accounts);
        assertEquals(accounts, service.findAll(sort));
        verify(repository, times(1)).findAll(sort);
    }

    @Test
    public void save() throws ServiceException {
        when(repository.save(account)).thenReturn(account);
        assertEquals(account, service.save(account));
        verify(repository, times(1)).save(account);
    }

    @Test
    public void saveAll() {
        List<Account> accounts = Collections.emptyList();
        when(repository.saveAll(accounts)).thenReturn(accounts);
        assertEquals(accounts, service.saveAll(accounts));
        verify(repository, times(1)).saveAll(accounts);
    }

    @Test
    public void deleteById() {
        doNothing().when(repository).deleteById(id);
        service.deleteById(id);
        verify(repository, times(1)).deleteById(id);
    }

    @Test
    public void delete() {
        doNothing().when(repository).delete(account);
        service.delete(account);
        verify(repository, times(1)).delete(account);
    }

    @Test
    public void deleteAll_iterable() {
        List<Account> accounts = Collections.emptyList();
        doNothing().when(repository).deleteAll(accounts);
        service.deleteAll(accounts);
        verify(repository, times(1)).deleteAll(accounts);
    }

    @Test
    public void deleteAll() {
        doNothing().when(repository).deleteAll();
        service.deleteAll();
        verify(repository, times(1)).deleteAll();
    }

    @Test
    public void deleteInBatch() {
        List<Account> accounts = Collections.emptyList();
        doNothing().when(repository).deleteInBatch(accounts);
        service.deleteInBatch(accounts);
        verify(repository, times(1)).deleteInBatch(accounts);
    }

    @Test
    public void deleteAllInBatch() {
        doNothing().when(repository).deleteAllInBatch();
        service.deleteAllInBatch();
        verify(repository, times(1)).deleteAllInBatch();
    }

    @Test
    public void findAll_byPerson() {
        List<Account> expected = List.of(account);
        when(repository.findAllByPersonOrderByName(person)).thenReturn(expected);
        List<Account> actual = service.findAll(person);
        assertEquals(expected, actual);
        verify(repository, times(1)).findAllByPersonOrderByName(person);
    }

    @Test
    public void findByNameAndPerson() {
        Optional<Account> expected = Optional.of(account);
        when(repository.findByNameAndPerson("Bank", person)).thenReturn(expected);
        assertEquals(expected, service.findByNameAndPerson("Bank", person));
        verify(repository, times(1)).findByNameAndPerson("Bank", person);
    }

    @Test
    public void getTotalBalance() {
        BigDecimal decimal = new BigDecimal("211.5600000000");
        when(repository.getTotalBalance(person)).thenReturn(decimal);
        assertEquals(decimal, repository.getTotalBalance(person));
    }
}