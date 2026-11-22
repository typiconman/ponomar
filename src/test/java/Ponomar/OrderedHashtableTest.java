package Ponomar;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.ComponentOrientation;
import java.util.*;
import org.junit.jupiter.api.Test;

class OrderedHashtableTest {
    @Test void cloneShouldPreserveLongValues() {
        OrderedHashtable source = new OrderedHashtable();
        source.put("days", 42L);
        OrderedHashtable clone = assertDoesNotThrow(source::clone);
        assertEquals(42L, clone.get("days"));
    }
    @Test void preservesInsertionOrderInAllViews() {
        OrderedHashtable table = table();
        assertEquals(List.of("first", "second", "third"), keys(table));
        assertEquals(List.of("one", "two", "three"), new ArrayList(table.values()));
        assertEquals(List.of("first", "second", "third"), new ArrayList(table.keySet()));
        assertEquals(3, table.keySet().size());
    }

    @Test void updatesWithoutReordering() {
        OrderedHashtable table = table();
        assertEquals("two", table.put("second", "updated"));
        assertEquals(List.of("first", "second", "third"), keys(table));
        assertEquals(List.of("one", "updated", "three"), new ArrayList(table.values()));
    }

    @Test void removeAndReinsertMovesKeyToEnd() {
        OrderedHashtable table = table();
        assertEquals("two", table.remove("second"));
        table.put("second", "new two");
        assertEquals(List.of("first", "third", "second"), keys(table));
        table.clear();
        assertTrue(table.isEmpty());
        assertTrue(keys(table).isEmpty());
    }

    @Test void supportsBulkOperationsAndViewRemoval() {
        OrderedHashtable table = table();
        Hashtable additions = new Hashtable();
        additions.put("fourth", "four");
        additions.put("fifth", "five");
        table.putAll(additions);
        assertTrue(table.keySet().containsAll(List.of("first", "fourth", "fifth")));
        assertTrue(table.values().contains("five"));
        assertTrue(table.keySet().remove("first"));
        assertFalse(table.containsKey("first"));
    }

    @Test void putAllFromOrderedMapPreservesItsIterationOrder() {
        OrderedHashtable destination = new OrderedHashtable();
        destination.put("existing", "old");
        destination.put("first", "one");
        OrderedHashtable additions = new OrderedHashtable();
        additions.put("existing", "new");
        additions.put("third", "three");
        additions.put("second", "two");

        destination.putAll(additions);

        assertEquals("new", destination.get("existing"));
        assertEquals(List.of("existing", "first", "third", "second"), keys(destination));
    }

    @Test void iteratorRejectsReadsPastEnd() {
        OrderedHashtable table = new OrderedHashtable();
        table.put("only", "value");
        Iterator iterator = table.iterateKeys();
        assertEquals("only", iterator.next());
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test void iteratorRejectsUnsupportedRemoval() {
        OrderedHashtable table = new OrderedHashtable();
        table.put("only", "value");
        assertThrows(UnsupportedOperationException.class, table.iterateKeys()::remove);
    }

    @Test void enumerationPreservesInsertionOrder() {
        Enumeration enumeration = table().enumerateKeys();
        List<Object> result = new ArrayList<>();
        while (enumeration.hasMoreElements()) result.add(enumeration.nextElement());
        assertEquals(List.of("first", "second", "third"), result);
        assertThrows(NoSuchElementException.class, enumeration::nextElement);
    }

    @Test void cloneHasIndependentMappings() {
        OrderedHashtable original = new OrderedHashtable();
        original.put("name", "Ponomar");
        original.put("year", 2024);
        OrderedHashtable clone = original.clone();
        clone.put("name", "Changed");
        clone.put("extra", "value");
        assertEquals("Ponomar", original.get("name"));
        assertFalse(original.containsKey("extra"));
        assertEquals(List.of("name", "year"), keys(original));
        assertEquals(List.of("name", "year", "extra"), keys(clone));
    }

    @Test void clonePreservesLocaleAndComponentOrientationObjects() {
        OrderedHashtable original = new OrderedHashtable();
        original.put("Locale", Locale.CANADA_FRENCH);
        original.put("Orient", ComponentOrientation.RIGHT_TO_LEFT);
        OrderedHashtable clone = original.clone();
        assertSame(Locale.CANADA_FRENCH, clone.get("Locale"));
        assertSame(ComponentOrientation.RIGHT_TO_LEFT, clone.get("Orient"));
    }

    @Test void valuesIteratorExplicitlyRejectsRemoval() {
        Iterator values = table().values().iterator();
        assertEquals("one", values.next());
        assertThrows(UnsupportedOperationException.class, values::remove);
    }

    private static OrderedHashtable table() {
        OrderedHashtable table = new OrderedHashtable();
        table.put("first", "one"); table.put("second", "two"); table.put("third", "three");
        return table;
    }

    private static List<Object> keys(OrderedHashtable table) {
        List<Object> keys = new ArrayList<>();
        table.iterateKeys().forEachRemaining(keys::add);
        return keys;
    }
}
