package daa.ds;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MinHeapTest {

    @Test
    void emptyHeapThrows() {
        MinHeap h = new MinHeap();
        assertTrue(h.isEmpty());
        assertThrows(IllegalStateException.class, h::peekMin);
        assertThrows(IllegalStateException.class, h::extractMin);
    }

    @Test
    void singleElement() {
        MinHeap h = new MinHeap();
        h.insert(5);
        assertEquals(5, h.peekMin());
        assertEquals(1, h.size());          // peekMin does not remove
        assertEquals(5, h.extractMin());
        assertTrue(h.isEmpty());
        assertThrows(IllegalStateException.class, h::extractMin);
    }

    @Test
    void duplicates() {
        MinHeap h = new MinHeap(2);
        for (int v : new int[]{3, 1, 3, 1, 2, 2, 3}) h.insert(v);
        int[] out = new int[7];
        for (int i = 0; i < 7; i++) out[i] = h.extractMin();
        assertArrayEquals(new int[]{1, 1, 2, 2, 3, 3, 3}, out);
    }

    @Test
    void heapPropertyHoldsAfterEveryInsertAndExtract() {
        Random rnd = new Random(42);
        MinHeap h = new MinHeap();
        for (int i = 0; i < 2000; i++) {
            h.insert(rnd.nextInt(1000) - 500);
            assertTrue(h.isValidHeap(), "after insert #" + i);
        }
        while (!h.isEmpty()) {
            h.extractMin();
            assertTrue(h.isValidHeap());
        }
    }

    @Test
    void extractMinReturnsSortedOutput() {
        Random rnd = new Random(42);
        int[] data = new int[10_000];
        for (int i = 0; i < data.length; i++) data[i] = rnd.nextInt();
        MinHeap h = new MinHeap();
        for (int v : data) h.insert(v);
        int[] expected = data.clone();
        Arrays.sort(expected);
        for (int e : expected) assertEquals(e, h.extractMin());
    }

    @Test
    void peekMinAlwaysShowsTheMinimum() {
        MinHeap h = new MinHeap();
        h.insert(10);
        assertEquals(10, h.peekMin());
        h.insert(4);
        assertEquals(4, h.peekMin());
        h.insert(7);
        assertEquals(4, h.peekMin());
    }

    @Test
    void buildHeapMatchesRepeatedInsert() {
        Random rnd = new Random(42);
        int[] data = new int[5000];
        for (int i = 0; i < data.length; i++) data[i] = rnd.nextInt(1000);
        int[] original = data.clone();
        MinHeap h = new MinHeap();
        h.buildHeap(data);
        assertArrayEquals(original, data, "buildHeap must not modify its argument");
        assertTrue(h.isValidHeap());
        int[] expected = data.clone();
        Arrays.sort(expected);
        for (int e : expected) assertEquals(e, h.extractMin());
    }

    @Test
    void buildHeapUsesAtMostTwoNComparisons() {
        int n = 100_000;
        int[] data = new Random(42).ints(n).toArray();
        MinHeap built = new MinHeap();
        built.buildHeap(data);
        assertTrue(built.metrics().comparisons <= 2L * n, "Floyd: at most 2n comparisons in siftDown");
    }
}
