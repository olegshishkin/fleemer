package com.fleemer.model;

import com.fleemer.model.enums.AccountType;
import com.fleemer.model.enums.CategoryType;
import com.fleemer.model.enums.Currency;
import java.math.BigDecimal;
import java.time.LocalDate;

public class EntityCreator {
    public static Account createAccount(Long id, AccountType type, Currency currency, String name, BigDecimal sum,
                                        Person person) {
        Account a = new Account();
        a.setId(id);
        a.setType(type);
        a.setCurrency(currency);
        a.setName(name);
        a.setBalance(sum);
        a.setPerson(person);
        return a;
    }

    public static Category createCategory(Long id, String name, CategoryType type, Person person) {
        Category c = new Category();
        c.setId(id);
        c.setPerson(person);
        c.setName(name);
        c.setType(type);
        return c;
    }

    public static Operation createOperation(Long id, LocalDate date, Account inAccount, Account outAccount,
                                            Category category, double sum, String comment) {
        Operation o = new Operation();
        o.setId(id);
        o.setDate(date);
        o.setInAccount(inAccount);
        o.setOutAccount(outAccount);
        o.setCategory(category);
        o.setSum(new BigDecimal(String.valueOf(sum)));
        o.setComment(comment);
        return o;
    }

    public static Person createPerson(Long id, String firstName, String lastName, String email, String hash) {
        Person p = new Person();
        p.setId(id);
        p.setFirstName(firstName);
        p.setLastName(lastName);
        p.setEmail(email);
        p.setHash(hash);
        return p;
    }
}
