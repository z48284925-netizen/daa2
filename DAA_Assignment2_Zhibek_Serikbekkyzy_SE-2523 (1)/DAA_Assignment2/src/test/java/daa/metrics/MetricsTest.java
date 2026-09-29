package daa.metrics;

import daa.ds.DynamicArray;
import daa.ds.MinHeap;
import daa.ds.MyLinkedList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Checks that the counters are incremented inside the operations and are not zero/estimated. */
class MetricsTest {

    private static DynamicArray filledArray(int n) {
        DynamicArray a = new DynamicArray();
        for (int i = 0; i < n; i++) a.add(i);
        a.metrics().reset();
        return a;
    }

    private static MyLinkedList filledList(int n) {
        MyLinkedList l = new MyLinkedList();
        for (int i = 0; i < n; i++) l.add(i);
        l.metrics().reset();
        return l;
    }

    @Test
    void resetSetsAllCountersToZero() {
        Metrics m = new Metrics();
        m.steps = 3;
        m.moves = 4;
        m.comparisons = 5;
        m.reset();
        assertEquals(0, m.steps + m.moves + m.comparisons);
    }

    @Test
    void arrayGetCostsExactlyOneStep() {
        DynamicArray a = filledArray(100);
        a.get(57);
        assertEquals(1, a.metrics().steps);
        assertEquals(0, a.metrics().moves);
    }

    @Test
    void listGetCostsIndexPlusOneSteps() {
        MyLinkedList l = filledList(100);
        l.get(57);
        assertEquals(58, l.metrics().steps);
    }

    @Test
    void arrayInsertAtHeadShiftsEveryElement() {
        DynamicArray a = new DynamicArray(1000);
        for (int i = 0; i < 100; i++) a.add(i);
        a.metrics().reset();
        a.add(0, -1);
        assertEquals(100, a.metrics().moves);
    }

    @Test
    void arrayInsertInMiddleShiftsHalf() {
        DynamicArray a = new DynamicArray(1000);
        for (int i = 0; i < 100; i++) a.add(i);
        a.metrics().reset();
        a.add(50, -1);
        assertEquals(50, a.metrics().moves);
    }

    @Test
    void arrayGrowthCopiesAllElements() {
        DynamicArray a = new DynamicArray(4);
        for (int i = 0; i < 4; i++) a.add(i);
        a.metrics().reset();
        a.add(4);                                  // array is full -> grows 2x, copies 4 elements
        assertEquals(4, a.metrics().moves);
    }

    @Test
    void listCountersAreNotZeroForInsertAndRemove() {
        MyLinkedList l = filledList(100);
        l.add(0, 1);
        assertEquals(2, l.metrics().moves);        // node.next and head
        assertEquals(0, l.metrics().steps);
        l.metrics().reset();
        l.add(50, 1);
        assertTrue(l.metrics().steps > 0 && l.metrics().moves == 2);
        l.metrics().reset();
        l.remove(50);
        assertTrue(l.metrics().steps >= 51 && l.metrics().moves >= 1);
    }

    @Test
    void containsCountsComparisonsInBothStructures() {
        DynamicArray a = filledArray(50);
        MyLinkedList l = filledList(50);
        assertFalse(a.contains(-1));
        assertFalse(l.contains(-1));
        assertEquals(50, a.metrics().comparisons);
        assertEquals(50, l.metrics().comparisons);
        assertEquals(50, a.metrics().steps);
        assertEquals(50, l.metrics().steps);
    }

    @Test
    void heapCountsComparisonsAndMoves() {
        MinHeap h = new MinHeap();
        for (int i = 0; i < 1000; i++) h.insert(i);           // ascending input: no bubble-up moves
        assertEquals(999, h.metrics().comparisons);       // exactly one comparison per insert (except the first)
        assertEquals(1000, h.size());
        h.metrics().reset();
        h.extractMin();
        assertTrue(h.metrics().comparisons > 0 && h.metrics().moves > 0);
    }

    @Test
    void csvLineAndHeader(@TempDir Path dir) throws IOException {
        Result r = new Result("W1_random_access", "-", "DynamicArray", 100, 0.5, 10, 20, 30);
        assertEquals("W1_random_access,-,DynamicArray,100,0.500,10,20,30", r.toCsvLine());
        Path file = dir.resolve("out").resolve("results.csv");
        CsvWriter.write(file, List.of(r));
        List<String> lines = Files.readAllLines(file);
        assertEquals("workload,variant,structure,n,time_ms,steps,moves,comparisons", lines.get(0));
        assertEquals(2, lines.size());
    }
}
