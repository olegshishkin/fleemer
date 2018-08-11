package com.fleemer.model;

import static com.fleemer.model.EntityCreator.createAccount;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.fleemer.model.enums.AccountType;
import com.fleemer.model.enums.Currency;
import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;

public class AccountTest {
    private Account a1;
    private Account a2;

    @Before
    public void setUp() {
        Person p1 = new Person();
        p1.setId(101L);
        p1.setEmail("email1");
        Person p2 = new Person();
        p2.setId(99L);
        p2.setEmail("email2");
        a1 = createAccount(11L, AccountType.CASH, Currency.RUB, "Wallet", new BigDecimal(1234.56), p1);
        a2 = createAccount(22L, AccountType.BANK_ACCOUNT, Currency.USD, "Depo", new BigDecimal(-0.99), p2);
    }

    @Test
    public void equals_null() {
        assertNotEquals(new Account(), null);
        assertEquals(new Account(), new Account());
        a1.setId(null);
        assertEquals(a1, new Account());
    }

    @Test
    public void equals_notNull() {
        assertNotEquals(a1, a2);
        a2.setId(11L);
        assertEquals(a1, a2);
    }

    @Test
    public void hashCode_test() {
        int hash = a1.hashCode();
        assertNotEquals(hash, a2.hashCode());
        a2.setId(11L);
        assertEquals(hash, a2.hashCode());
    }

    @Test
    public void getRefactoredType_success() {
        Account a = createAccount(11L, AccountType.CASH, Currency.USD, "Wallet", BigDecimal.TEN, null);
        assertEquals("Cash", a.getRefactoredType());
        a.setType(AccountType.BANK_ACCOUNT);
        assertEquals("Bank account", a.getRefactoredType());
    }
}