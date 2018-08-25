package com.fleemer.repository;

import com.fleemer.model.*;
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
    private List<Confirmation> confirmations;

    RepositoryTestingPopulationClass() {
        accounts = getTestAccounts();
        people = getTestPeople();
        categories = getTestCategories();
        operations = getTestOperations();
        confirmations = getTestConfirmations();
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

    public List<Confirmation> getConfirmations() {
        return confirmations;
    }

    private List<Person> getTestPeople() {
        Person p1 = EntityCreator.create(1L, "FirstName1", "LastName1", "mail100@mail.ma", "hash1", 0);
        Person p2 = EntityCreator.create(2L, "FirstName2", "LastName2", "mail95@mail.ma", "hash2", 0);
        Person p3 = EntityCreator.create(3L, "FirstName3", "LastName3", "mail3@mail.ma", "hash3", 0);
        Person p4 = EntityCreator.create(4L, "FirstName4", "LastName4", "mail5@mail.ma", "hash4", 0);
        Person p5 = EntityCreator.create(5L, "FirstName5", "LastName5", "mail@mail.ma", "hash5", 0);
        return List.of(p1, p2, p3, p4, p5);
    }

    private List<Account> getTestAccounts() {
        List<Person> people = getTestPeople();
        BigDecimal sum1 = new BigDecimal("00000000000000001012.1000000000");
        Account a1 = EntityCreator.create(1L, AccountType.CASH, Currency.RUB, "Cash", sum1, people.get(1), 0);
        BigDecimal sum2 = new BigDecimal("00000000000000000333.4500000000");
        Account a2 = EntityCreator.create(2L, AccountType.BANK_ACCOUNT, Currency.USD, "Bank", sum2, people.get(3), 0);
        BigDecimal sum3 = new BigDecimal("00000000000000001001.0200000000");
        Account a3 = EntityCreator.create(3L, AccountType.DEBT, Currency.RUB, "Wallet", sum3, people.get(0), 0);
        return List.of(a1, a2, a3);
    }

    private List<Operation> getTestOperations() {
        List<Account> accounts = getTestAccounts();
        List<Category> categories = getTestCategories();
        Operation o1 = EntityCreator.create(1L, LocalDate.of(2018, Month.MAY, 3), accounts.get(1), null, categories.get(6),
                4567.98, "comment1", 0);
        Operation o2 = EntityCreator.create(2L, LocalDate.of(2018, Month.JUNE, 12), null, accounts.get(0), categories.get(5),
                334.09, "comment2", 0);
        Operation o3 = EntityCreator.create(3L, LocalDate.of(2016, Month.MAY, 12), accounts.get(2), accounts.get(0), null,
                8080.11, "comment3", 0);
        Operation o4 = EntityCreator.create(4L, LocalDate.of(2018, Month.MAY, 1), null, accounts.get(0), categories.get(4),
                61.32, "comment4", 0);
        Operation o5 = EntityCreator.create(5L, LocalDate.of(2018, Month.NOVEMBER, 12), accounts.get(0), accounts.get(1),
                null, 543.0, "comment5", 0);
        Operation o6 = EntityCreator.create(6L, LocalDate.of(2017, Month.MAY, 12), accounts.get(1), null, categories.get(6),
                12.1, "comment6", 0);
        Operation o7 = EntityCreator.create(7L, LocalDate.of(2018, Month.DECEMBER, 12), accounts.get(0), null,
                categories.get(5), 146.58, "comment7", 0);
        Operation o8 = EntityCreator.create(8L, LocalDate.of(2015, Month.MAY, 8), accounts.get(0), accounts.get(1), null,
                1176.92, "comment8", 0);
        Operation o9 = EntityCreator.create(9L, LocalDate.of(2018, Month.MAY, 12), null, accounts.get(0), categories.get(3),
                6.03, "comment9", 0);
        return List.of(o1, o2, o3, o4, o5, o6, o7, o8, o9);
    }

    private List<Category> getTestCategories() {
        List<Person> people = getTestPeople();
        Category c1 = EntityCreator.create(1L, "Car", CategoryType.OUTCOME, people.get(0), 0);
        Category c2 = EntityCreator.create(2L, "Home", CategoryType.OUTCOME, people.get(2), 0);
        Category c3 = EntityCreator.create(3L, "Salary", CategoryType.INCOME, people.get(0), 0);
        Category c4 = EntityCreator.create(4L, "Car", CategoryType.OUTCOME, people.get(1), 0);
        Category c5 = EntityCreator.create(5L, "Gifts", CategoryType.OUTCOME, people.get(0), 0);
        Category c6 = EntityCreator.create(6L, "Home", CategoryType.OUTCOME, people.get(1), 0);
        Category c7 = EntityCreator.create(7L, "Salary", CategoryType.INCOME, people.get(3), 0);
        return List.of(c1, c2, c3, c4, c5, c6, c7);
    }

    private List<Confirmation> getTestConfirmations() {
        List<Person> people = getTestPeople();
        Confirmation c1 = EntityCreator.create(1L, "1c58fa7f-f3ce-4886-bce3-02be9c23fcdd", true, people.get(0), 0);
        Confirmation c2 = EntityCreator.create(2L, "2c58fa7f-f3ce-4886-bce3-02be9c23fcdd", true, people.get(1), 0);
        Confirmation c3 = EntityCreator.create(3L, "3c58fa7f-f3ce-4886-bce3-02be9c23fcdd", false, people.get(2), 0);
        return List.of(c1, c2, c3);
    }
}
