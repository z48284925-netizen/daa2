package daa.ds;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** The same tests run against DynamicArray and MyLinkedList (java.util is allowed in tests only). */
class IntListContractTest {

    static Stream<Arguments> lists() {
        return Stream.of(
                Arguments.of("DynamicArray", (Supplier<IntList>) DynamicArray::new),
                Arguments.of("MyLinkedList", (Supplier<IntList>) MyLinkedList::new));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("lists")
    void emptyStructure(String name, Supplier<IntList> f) {
        IntList l = f.get();
        assertEquals(0, l.size());
        assertFalse(l.contains(1));
        assertThrows(IndexOutOfBoundsException.class, () -> l.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> l.remove(0));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("lists")
    void singleElement(String name, Supplier<IntList> f) {
        IntList l = f.get();
        l.add(7);
        assertEquals(1, l.size());
        assertEquals(7, l.get(0));
        assertTrue(l.contains(7));
        assertEquals(7, l.remove(0));
        assertEquals(0, l.size());
        l.add(8);                                   // list must still work after becoming empty
        assertEquals(8, l.get(0));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("lists")
    void duplicatesAndFirstLastIndex(String name, Supplier<IntList> f) {
        IntList l = f.get();
        for (int v : new int[]{5, 5, 1, 5, 9}) l.add(v);
        assertEquals(5, l.size());
        assertEquals(5, l.get(0));                  // first
        assertEquals(9, l.get(4));                  // last
        assertEquals(5, l.remove(0));               // remove first
        assertEquals(9, l.remove(3));               // remove last
        assertEquals(3, l.size());
        assertEquals(5, l.get(0));
        assertEquals(1, l.get(1));
        assertEquals(5, l.get(2));
        l.add(3, 42);                               // insert at index == size (append)
        assertEquals(42, l.get(3));
        l.add(0, 100);                              // insert at head
        assertEquals(100, l.get(0));
        assertEquals(5, l.size() );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("lists")
    void invalidIndexThrows(String name, Supplier<IntList> f) {
        IntList l = f.get();
        l.add(1);
        l.add(2);
        assertThrows(IndexOutOfBoundsException.class, () -> l.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> l.get(2));
        assertThrows(IndexOutOfBoundsException.class, () -> l.add(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> l.add(3, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> l.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> l.remove(2));
        assertEquals(2, l.size());                  // failed calls must not change the structure
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("lists")
    void growsFarBeyondInitialCapacity(String name, Supplier<IntList> f) {
        IntList l = f.get();
        for (int i = 0; i < 5000; i++) l.add(i);
        assertEquals(5000, l.size());
        for (int i = 0; i < 5000; i++) assertEquals(i, l.get(i));
        assertTrue(l.contains(4999));
        assertFalse(l.contains(5000));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("lists")
    void randomOperationsMatchArrayList(String name, Supplier<IntList> f) {
        IntList l = f.get();
        ArrayList<Integer> expected = new ArrayList<>();
        Random rnd = new Random(42);
        for (int step = 0; step < 20_000; step++) {
            int op = rnd.nextInt(5);
            int value = rnd.nextInt(50);
            switch (op) {
                case 0 -> {
                    l.add(value);
                    expected.add(value);
                }
                case 1 -> {
                    int idx = rnd.nextInt(expected.size() + 1);
                    l.add(idx, value);
                    expected.add(idx, value);
                }
                case 2 -> {
                    if (!expected.isEmpty()) {
                        int idx = rnd.nextInt(expected.size());
                        assertEquals(expected.remove(idx), l.remove(idx));
                    }
                }
                case 3 -> {
                    if (!expected.isEmpty()) {
                        int idx = rnd.nextInt(expected.size());
                        assertEquals(expected.get(idx), l.get(idx));
                    }
                }
                default -> assertEquals(expected.contains(value), l.contains(value));
            }
            assertEquals(expected.size(), l.size());
        }
        for (int i = 0; i < expected.size(); i++) assertEquals(expected.get(i), l.get(i));
    }

    @org.junit.jupiter.api.Test
    void fixedArrayBaselineRejectsOverflow() {
        FixedIntArray a = new FixedIntArray(2);
        a.add(1);
        a.add(2);
        Assertions.assertThrows(IllegalStateException.class, () -> a.add(3));
    }
}
