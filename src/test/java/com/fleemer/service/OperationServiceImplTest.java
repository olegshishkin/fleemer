package com.fleemer.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import com.fleemer.model.Account;
import com.fleemer.model.Category;
import com.fleemer.model.Operation;
import com.fleemer.model.Person;
import com.fleemer.model.enums.CategoryType;
import com.fleemer.repository.OperationRepository;
import com.fleemer.service.exception.ServiceException;
import com.fleemer.service.implementation.OperationServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
    private AccountService accountService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private OperationRepository repository;

    @Mock
    private Operation operation;

    @Mock
    private Pageable pageable;

    @Mock
    private Person person;

    @Mock
    private Page<Operation> page;

    @Mock
    private Sort sort;

    @Mock
    private Account account;

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
    public void delete() throws ServiceException {
        doNothing().when(repository).delete(operation);
        when(operation.getInAccount()).thenReturn(account);
        when(operation.getOutAccount()).thenReturn(account);
        when(operation.getSum()).thenReturn(BigDecimal.TEN);
        when(repository.getOne(any())).thenReturn(operation);
        when(account.getBalance()).thenReturn(BigDecimal.ZERO);
        service.delete(operation);
        verify(repository, times(1)).delete(operation);
        verify(operation, times(1)).getInAccount();
        verify(operation, times(1)).getOutAccount();
        verify(operation, times(1)).getSum();
        verify(repository, times(1)).getOne(any());
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

    @Test
    public void findAll_person() throws ServiceException {
        List<Operation> expected = Collections.emptyList();
        when(repository.findAllByInAccountPersonOrOutAccountPerson(person, person)).thenReturn(expected);
        assertEquals(expected, service.findAllByPerson(person, null, null));
        verify(repository, times(1)).findAllByInAccountPersonOrOutAccountPerson(person, person);
    }

    @Test
    public void findAll_personAndPageable() throws ServiceException {
        when(repository.findAllByInAccountPersonOrOutAccountPerson(person, person, pageable)).thenReturn(page);
        assertEquals(page, service.findAllByPerson(person, null, null, pageable));
        verify(repository, times(1)).findAllByInAccountPersonOrOutAccountPerson(person, person, pageable);
    }

    @Test
    public void save_outcomeNull() throws ServiceException {
        Category category = new Category();
        category.setType(CategoryType.INCOME);

        Account in = new Account();
        in.setBalance(BigDecimal.valueOf(10.01));

        Operation operation = new Operation();
        operation.setId(11L);
        operation.setSum(BigDecimal.valueOf(3.45));
        operation.setCategory(category);
        operation.setInAccount(in);
        when(repository.save(operation)).thenReturn(operation);
        when(accountService.save(in)).thenReturn(in);
        when(categoryService.save(category)).thenReturn(category);
        assertEquals(operation, service.save(operation));
        assertEquals(13.46, in.getBalance().doubleValue(), 0.0);
        verify(repository, times(1)).save(operation);
    }

    @Test
    public void save_incomeNull() throws ServiceException {
        Category category = new Category();
        category.setType(CategoryType.OUTCOME);

        Account out = new Account();
        out.setBalance(BigDecimal.valueOf(10.01));

        Operation operation = new Operation();
        operation.setId(11L);
        operation.setSum(BigDecimal.valueOf(3.45));
        operation.setCategory(category);
        operation.setOutAccount(out);
        when(repository.save(operation)).thenReturn(operation);
        when(accountService.save(out)).thenReturn(out);
        when(categoryService.save(category)).thenReturn(category);
        assertEquals(operation, service.save(operation));
        assertEquals(6.56, out.getBalance().doubleValue(), 0.0);
        verify(repository, times(1)).save(operation);
    }

    @Test
    public void save_categoryNull() throws ServiceException {
        Account in = new Account();
        in.setBalance(BigDecimal.valueOf(6.09));

        Account out = new Account();
        out.setBalance(BigDecimal.valueOf(10.01));

        Operation operation = new Operation();
        operation.setId(11L);
        operation.setSum(BigDecimal.valueOf(3.45));
        operation.setInAccount(in);
        operation.setOutAccount(out);
        when(repository.save(operation)).thenReturn(operation);
        when(accountService.save(in)).thenReturn(in);
        when(accountService.save(out)).thenReturn(out);
        assertEquals(operation, service.save(operation));
        assertEquals(9.54, in.getBalance().doubleValue(), 0.0);
        assertEquals(6.56, out.getBalance().doubleValue(), 0.0);
        verify(repository, times(1)).save(operation);
    }

    @Test(expected = ServiceException.class)
    public void save_bothAccountsNull() throws ServiceException {
        String errorMessage = "Both the income account and outcome account are missing.";
        Category category = new Category();
        category.setType(CategoryType.INCOME);
        Operation operation = new Operation();
        operation.setId(11L);
        operation.setSum(BigDecimal.valueOf(3.45));
        operation.setCategory(category);
        try {
            service.save(operation);
        } catch (ServiceException e) {
            assertEquals(e.getMessage(), errorMessage);
            throw e;
        }
    }

    @Test(expected = ServiceException.class)
    public void save_categoryAndOneAccountNull() throws ServiceException {
        String errorMessage = "The category and at least one account are missing.";
        Account in = new Account();
        in.setBalance(BigDecimal.valueOf(6.09));
        Operation operation = new Operation();
        operation.setId(11L);
        operation.setSum(BigDecimal.valueOf(3.45));
        operation.setInAccount(in);
        try {
            service.save(operation);
        } catch (ServiceException e) {
            assertEquals(e.getMessage(), errorMessage);
            throw e;
        }
    }

    @Test(expected = ServiceException.class)
    public void save_allNotNull() throws ServiceException {
        String errorMessage = "Category and both the accounts are not null. There is no way to determine an operation type.";
        Category category = new Category();
        category.setType(CategoryType.OUTCOME);

        Account in = new Account();
        in.setBalance(BigDecimal.valueOf(6.09));

        Account out = new Account();
        out.setBalance(BigDecimal.valueOf(10.01));

        Operation operation = new Operation();
        operation.setId(11L);
        operation.setSum(BigDecimal.valueOf(3.45));
        operation.setCategory(category);
        operation.setInAccount(in);
        operation.setOutAccount(out);
        try {
            service.save(operation);
        } catch (ServiceException e) {
            assertEquals(e.getMessage(), errorMessage);
            throw e;
        }
    }

    @Test(expected = ServiceException.class)
    public void save_wrongIncomeType() throws ServiceException {
        String errorMessage = "Wrong category type for that operation: INCOME.";
        Category category = new Category();
        category.setType(CategoryType.INCOME);

        Account out = new Account();
        out.setBalance(BigDecimal.valueOf(10.01));

        Operation operation = new Operation();
        operation.setId(11L);
        operation.setSum(BigDecimal.valueOf(3.45));
        operation.setCategory(category);
        operation.setOutAccount(out);
        try {
            service.save(operation);
        } catch (ServiceException e) {
            assertEquals(e.getMessage(), errorMessage);
            throw e;
        }
    }

    @Test(expected = ServiceException.class)
    public void save_wrongOutcomeType() throws ServiceException {
        String errorMessage = "Wrong category type for that operation: OUTCOME.";
        Category category = new Category();
        category.setType(CategoryType.OUTCOME);

        Account in = new Account();
        in.setBalance(BigDecimal.valueOf(10.01));

        Operation operation = new Operation();
        operation.setId(11L);
        operation.setSum(BigDecimal.valueOf(3.45));
        operation.setCategory(category);
        operation.setInAccount(in);
        try {
            service.save(operation);
        } catch (ServiceException e) {
            assertEquals(e.getMessage(), errorMessage);
            throw e;
        }
    }

    @Test
    public void findByIdAndPerson() {
        long id = 11L;
        Optional<Operation> expected = Optional.of(operation);
        when(repository.getByIdAndInAccountPersonOrOutAccountPerson(id, person, person)).thenReturn(expected);
        assertEquals(expected, service.findByIdAndPerson(id, person));
        verify(repository, times(1)).getByIdAndInAccountPersonOrOutAccountPerson(id, person, person);
    }

    @Test
    public void countOperationsByCategory() {
        Category category = new Category();
        when(repository.countOperationsByCategory(category)).thenReturn(11L);
        assertEquals(11L, service.countOperationsByCategory(category));
        verify(repository, times(1)).countOperationsByCategory(category);
    }

    @Test
    public void countOperationsByAccounts() {
        Account account = new Account();
        when(repository.countOperationsByInAccountOrOutAccount(account, account)).thenReturn(11L);
        assertEquals(11L, service.countOperationsByAccount(account));
        verify(repository, times(1)).countOperationsByInAccountOrOutAccount(account, account);
    }

    @Test
    public void findAllDailyVolumes() {
        LocalDate from = LocalDate.of(2018, 1, 1);
        LocalDate till = LocalDate.of(2018, 12, 31);
        List<Object[]> expected = new ArrayList<>();
        when(repository.findAllDailyVolumes(from, till, person)).thenReturn(expected);
        assertEquals(expected, service.findAllDailyVolumes(from, till, person));
        verify(repository, times(1)).findAllDailyVolumes(from, till, person);
    }
}