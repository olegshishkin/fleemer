package com.fleemer.model;

import static com.fleemer.model.EntityCreator.create;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.time.*;
import org.junit.Before;
import org.junit.Test;

public class OperationTest {
    private Operation o1;
    private Operation o2;

    @Before
    public void setUp() {
        Account in1 = new Account();
        in1.setId(1111L);
        Account out1 = new Account();
        out1.setId(11111L);
        Category c1 = new Category();
        c1.setId(3333L);
        o1 = create(11L, LocalDate.of(1990, Month.JANUARY, 1), in1, out1, c1, 123.12, "comment1", 1);

        Account in2 = new Account();
        in2.setId(2222L);
        Account out2 = new Account();
        out2.setId(22222L);
        Category c2 = new Category();
        c2.setId(4444L);
        o2 = create(22L, LocalDate.of(2000, Month.MARCH, 8), in2, out2, c2, 0.008, "comment2", 1);
    }

    @Test
    public void equals_null() {
        assertNotEquals(new Operation(), null);
        assertEquals(new Operation(), new Operation());
        o1.setId(null);
        assertEquals(o1, new Operation());
    }

    @Test
    public void equals_notNull() {
        assertNotEquals(o1, o2);
        o2.setId(11L);
        assertEquals(o1, o2);
    }

    @Test
    public void hashCode_test() {
        int hash = o1.hashCode();
        assertNotEquals(hash, o2.hashCode());
        o2.setId(11L);
        assertEquals(hash, o2.hashCode());
    }
}