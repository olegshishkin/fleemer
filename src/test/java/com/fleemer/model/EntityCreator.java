package com.fleemer.model;

import com.fleemer.model.enums.AccountType;
import com.fleemer.model.enums.CategoryType;
import com.fleemer.model.enums.Currency;
import java.math.BigDecimal;
import java.time.LocalDate;

public class EntityCreator {
    public static Account create(Long id, AccountType type, Currency currency, String name, BigDecimal sum,
                                 Person person, int version) {
        Account a = new Account();
        a.setId(id);
        a.setType(type);
        a.setCurrency(currency);
        a.setName(name);
        a.setBalance(sum);
        a.setPerson(person);
        a.setVersion(version);
        return a;
    }

    public static Category create(Long id, String name, CategoryType type, Person person, int version) {
        Category c = new Category();
        c.setId(id);
        c.setPerson(person);
        c.setName(name);
        c.setType(type);
        c.setVersion(version);
        return c;
    }

    public static Operation create(Long id, LocalDate date, Account inAccount, Account outAccount,
                                   Category category, double sum, String comment, int version) {
        Operation o = new Operation();
        o.setId(id);
        o.setDate(date);
        o.setInAccount(inAccount);
        o.setOutAccount(outAccount);
        o.setCategory(category);
        o.setSum(new BigDecimal(String.valueOf(sum)));
        o.setComment(comment);
        o.setVersion(version);
        return o;
    }

    public static Person create(Long id, String firstName, String lastName, String email, String hash, int version) {
        Person p = new Person();
        p.setId(id);
        p.setFirstName(firstName);
        p.setLastName(lastName);
        p.setEmail(email);
        p.setHash(hash);
        p.setVersion(version);
        return p;
    }

    public static Confirmation create(Long id, String token, boolean enabled, Person person, int version) {
        Confirmation c = new Confirmation();
        c.setId(id);
        c.setToken(token);
        c.setEnabled(enabled);
        c.setPerson(person);
        c.setVersion(version);
        return c;
    }
}
