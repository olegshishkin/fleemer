package com.fleemer.repository;

import static com.fleemer.model.EntityCreator.*;

import com.fleemer.model.Account;
import com.fleemer.model.Category;
import com.fleemer.model.Operation;
import com.fleemer.model.Person;
import com.fleemer.model.enums.AccountType;
import com.fleemer.model.enums.CategoryType;
import com.fleemer.model.enums.Currency;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

class RepositoryTestingPopulationClass {
    private List<Account> accounts;
    private List<Person> people;
    private List<Category> categories;
    private List<Operation> operations;

    RepositoryTestingPopulationClass() {
        accounts = getTestAccounts();
        people = getTestPeople();
        categories = getTestCategories();
        operations = getTestOperations();
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public List<Person> getPeople() {
        return people;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    private List<Person> getTestPeople() {
        Person p1 = createPerson(1L, "FirstName1", "LastName1", "mail100@mail.ma", "hash1", 0);
        Person p2 = createPerson(2L, "FirstName2", "LastName2", "mail95@mail.ma", "hash2", 0);
        Person p3 = createPerson(3L, "FirstName3", "LastName3", "mail3@mail.ma", "hash3", 0);
        Person p4 = createPerson(4L, "FirstName4", "LastName4", "mail5@mail.ma", "hash4", 0);
        Person p5 = createPerson(5L, "FirstName5", "LastName5", "mail@mail.ma", "hash5", 0);
        return List.of(p1, p2, p3, p4, p5);
    }

    private List<Account> getTestAccounts() {
        List<Person> people = getTestPeople();
        BigDecimal sum1 = new BigDecimal("00000000000000001012.1000000000");
        Account a1 = createAccount(1L, AccountType.CASH, Currency.RUB, "Cash", sum1, people.get(1), 0);
        BigDecimal sum2 = new BigDecimal("00000000000000000333.4500000000");
        Account a2 = createAccount(2L, AccountType.BANK_ACCOUNT, Currency.USD, "Bank", sum2, people.get(3), 0);
        BigDecimal sum3 = new BigDecimal("00000000000000001001.0200000000");
        Account a3 = createAccount(3L, AccountType.DEBT, Currency.RUB, "Wallet", sum3, people.get(0), 0);
        return List.of(a1, a2, a3);
    }

    private List<Operation> getTestOperations() {
        List<Account> accounts = getTestAccounts();
        List<Category> categories = getTestCategories();
        Operation o1 = createOperation(1L, LocalDate.of(2018, Month.MAY, 3), accounts.get(1), null, categories.get(6),
                4567.98, "comment1", 0);
        Operation o2 = createOperation(2L, LocalDate.of(2018, Month.JUNE, 12), null, accounts.get(0), categories.get(5),
                334.09, "comment2", 0);
        Operation o3 = createOperation(3L, LocalDate.of(2016, Month.MAY, 12), accounts.get(2), accounts.get(0), null,
                8080.11, "comment3", 0);
        Operation o4 = createOperation(4L, LocalDate.of(2018, Month.MAY, 1), null, accounts.get(0), categories.get(4),
                61.32, "comment4", 0);
        Operation o5 = createOperation(5L, LocalDate.of(2018, Month.NOVEMBER, 12), accounts.get(0), accounts.get(1),
                null, 543.0, "comment5", 0);
        Operation o6 = createOperation(6L, LocalDate.of(2017, Month.MAY, 12), accounts.get(1), null, categories.get(6),
                12.1, "comment6", 0);
        Operation o7 = createOperation(7L, LocalDate.of(2018, Month.DECEMBER, 12), accounts.get(0), null,
                categories.get(5), 146.58, "comment7", 0);
        Operation o8 = createOperation(8L, LocalDate.of(2015, Month.MAY, 8), accounts.get(0), accounts.get(1), null,
                1176.92, "comment8", 0);
        Operation o9 = createOperation(9L, LocalDate.of(2018, Month.MAY, 12), null, accounts.get(0), categories.get(3),
                6.03, "comment9", 0);
        return List.of(o1, o2, o3, o4, o5, o6, o7, o8, o9);
    }

    private List<Category> getTestCategories() {
        List<Person> people = getTestPeople();
        Category c1 = createCategory(1L, "Car", CategoryType.OUTCOME, people.get(0), 0);
        Category c2 = createCategory(2L, "Home", CategoryType.OUTCOME, people.get(2), 0);
        Category c3 = createCategory(3L, "Salary", CategoryType.INCOME, people.get(0), 0);
        Category c4 = createCategory(4L, "Car", CategoryType.OUTCOME, people.get(1), 0);
        Category c5 = createCategory(5L, "Gifts", CategoryType.OUTCOME, people.get(0), 0);
        Category c6 = createCategory(6L, "Home", CategoryType.OUTCOME, people.get(1), 0);
        Category c7 = createCategory(7L, "Salary", CategoryType.INCOME, people.get(3), 0);
        return List.of(c1, c2, c3, c4, c5, c6, c7);
    }
}
