package com.fleemer.model;

import static com.fleemer.model.EntityCreator.createPerson;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Before;
import org.junit.Test;

public class PersonTest {
    private Person p1;
    private Person p2;

    @Before
    public void setUp() {
        p1 = createPerson(11L, "First", "Last", "my@mail.org", "hash");
        p2 = createPerson(22L, "First1", "Last1", "my1@mail.org", "hash1");
    }

    @Test
    public void equals_null() {
        assertNotEquals(new Person(), null);
        assertEquals(new Person(), new Person());
        p1.setId(null);
        p1.setEmail(null);
        assertEquals(p1, new Person());
    }

    @Test
    public void equals_notNull() {
        assertNotEquals(p1, p2);
        p2.setId(11L);
        assertNotEquals(p1, p2);
        p2.setEmail("my@mail.org");
        assertEquals(p1, p2);
    }

    @Test
    public void hashCode_test() {
        int hash = p1.hashCode();
        assertNotEquals(hash, p2.hashCode());
        p2.setId(11L);
        p2.setEmail("my@mail.org");
        assertEquals(hash, p2.hashCode());
    }
}