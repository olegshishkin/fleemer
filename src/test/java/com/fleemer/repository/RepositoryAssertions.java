package com.fleemer.repository;

import static org.hamcrest.Matchers.comparesEqualTo;

import com.fleemer.model.*;
import java.io.Serializable;
import java.util.Iterator;
import org.junit.Assert;

public class RepositoryAssertions {
    public static void assertEquals(Account expected, Account actual) {
        if (expected == null || actual == null) {
            Assert.assertTrue(expected == null & actual == null);
            return;
        }
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getType(), actual.getType());
        Assert.assertEquals(expected.getCurrency(), actual.getCurrency());
        Assert.assertEquals(expected.getName(), actual.getName());
        Assert.assertEquals(expected.getPerson(), actual.getPerson());
        Assert.assertThat(actual.getBalance(), comparesEqualTo(expected.getBalance()));
        Assert.assertEquals(expected.getVersion(), actual.getVersion());
    }

    public static void assertEquals(Person expected, Person actual) {
        if (expected == null || actual == null) {
            Assert.assertTrue(expected == null & actual == null);
            return;
        }
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getFirstName(), actual.getFirstName());
        Assert.assertEquals(expected.getLastName(), actual.getLastName());
        Assert.assertEquals(expected.getEmail(), actual.getEmail());
        Assert.assertEquals(expected.getHash(), actual.getHash());
        Assert.assertEquals(expected.getVersion(), actual.getVersion());
    }

    public static void assertEquals(Category expected, Category actual) {
        if (expected == null || actual == null) {
            Assert.assertTrue(expected == null & actual == null);
            return;
        }
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getName(), actual.getName());
        Assert.assertEquals(expected.getType(), actual.getType());
        Assert.assertEquals(expected.getPerson(), actual.getPerson());
        Assert.assertEquals(expected.getVersion(), actual.getVersion());
    }

    public static void assertEquals(Operation expected, Operation actual) {
        if (expected == null || actual == null) {
            Assert.assertTrue(expected == null & actual == null);
            return;
        }
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getDate(), actual.getDate());
        assertEquals(expected.getInAccount(), actual.getInAccount());
        assertEquals(expected.getOutAccount(), actual.getOutAccount());
        assertEquals(expected.getCategory(), actual.getCategory());
        Assert.assertThat(actual.getSum(), comparesEqualTo(expected.getSum()));
        Assert.assertEquals(expected.getComment(), actual.getComment());
        Assert.assertEquals(expected.getVersion(), actual.getVersion());
    }

    public static void assertEquals(Confirmation expected, Confirmation actual) {
        if (expected == null || actual == null) {
            Assert.assertTrue(expected == null & actual == null);
            return;
        }
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getToken(), actual.getToken());
        Assert.assertEquals(expected.getPerson(), actual.getPerson());
        Assert.assertEquals(expected.getVersion(), actual.getVersion());
    }

    public static <T extends Serializable> void assertIterableEquals(Iterable<T> expected, Iterable<T> actual) {
        if (expected == actual) {
            return;
        }
        Assert.assertTrue(expected != null & actual != null);
        Iterator<T> expectedIterator = expected.iterator();
        Iterator<T> actualIterator = actual.iterator();
        while (expectedIterator.hasNext() && actualIterator.hasNext()) {
            T expectedElement = expectedIterator.next();
            T actualElement = actualIterator.next();
            if (expectedElement == actualElement) {
                continue;
            }
            if (expectedElement instanceof Account) {
                assertEquals((Account) expectedElement, (Account) actualElement);
            } else if (expectedElement instanceof Person) {
                assertEquals((Person) expectedElement, (Person) actualElement);
            } else if (expectedElement instanceof Category) {
                assertEquals((Category) expectedElement, (Category) actualElement);
            } else if (expectedElement instanceof Operation) {
                assertEquals((Operation) expectedElement, (Operation) actualElement);
            } else if (expectedElement instanceof Confirmation) {
                assertEquals((Confirmation) expectedElement, (Confirmation) actualElement);
            } else {
                String msg = "Unknown class for comparison: ";
                throw new ClassCastException(msg + expectedElement.getClass().getCanonicalName());
            }
        }
        Assert.assertFalse(expectedIterator.hasNext());
        Assert.assertFalse(actualIterator.hasNext());
    }
}
