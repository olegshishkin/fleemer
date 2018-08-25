package com.fleemer.model;

import static com.fleemer.model.EntityCreator.create;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Before;
import org.junit.Test;

public class ConfirmationTest {
    private Confirmation c1;
    private Confirmation c2;

    @Before
    public void setUp() {
        Person p1 = new Person();
        p1.setId(101L);
        p1.setEmail("email1");
        c1 = create(11L, "token", true, p1, 1);
        Person p2 = new Person();
        p2.setId(99L);
        p2.setEmail("email2");
        c2 = create(22L, "token2", false, p2, 1);
    }

    @Test
    public void equals_null() {
        assertNotEquals(new Confirmation(), null);
        assertEquals(new Confirmation(), new Confirmation());
        c1.setId(null);
        assertEquals(c1, new Confirmation());
    }

    @Test
    public void equals_notNull() {
        assertNotEquals(c1, c2);
        c2.setId(11L);
        assertEquals(c1, c2);
    }

    @Test
    public void hashCode_test() {
        int hash = c1.hashCode();
        assertNotEquals(hash, c2.hashCode());
        c2.setId(11L);
        assertEquals(hash, c2.hashCode());
    }
}