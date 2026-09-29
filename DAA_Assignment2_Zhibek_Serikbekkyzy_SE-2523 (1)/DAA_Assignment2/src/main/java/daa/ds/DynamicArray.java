package daa.ds;

import daa.metrics.Metrics;

/**
 * Resizable array of primitive ints (no java.util, no boxing).
 * The backing array grows 2x when it is full, so add(x) is amortized O(1).
 */
public class DynamicArray implements IntList {
    private static final int DEFAULT_CAPACITY = 10;

    private int[] data;
    private int size;
    private final Metrics metrics = new Metrics();

    public DynamicArray() {
        this(DEFAULT_CAPACITY);
    }

    public DynamicArray(int initialCapacity) {
        if (initialCapacity < 1) initialCapacity = 1;
        data = new int[initialCapacity];
    }

    @Override
    public void add(int x) {
        ensureCapacity();
        data[size++] = x;
        metrics.steps++;                 // one array cell written
    }

    @Override
    public void add(int index, int x) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException("index=" + index + ", size=" + size);
        ensureCapacity();
        for (int i = size; i > index; i--) {   // shift right
            data[i] = data[i - 1];
            metrics.moves++;
        }
        data[index] = x;
        metrics.steps++;
        size++;
    }

    @Override
    public int remove(int index) {
        checkIndex(index);
        int removed = data[index];
        metrics.steps++;
        for (int i = index; i < size - 1; i++) {  // shift left
            data[i] = data[i + 1];
            metrics.moves++;
        }
        size--;
        return removed;
    }

    @Override
    public int get(int index) {
        checkIndex(index);
        metrics.steps++;
        return data[index];
    }

    /**
     * Linear search.
     * Loop invariant (see REPORT.md): before each iteration with counter i,
     * x does not occur in data[0..i-1].
     */
    @Override
    public boolean contains(int x) {
        for (int i = 0; i < size; i++) {
            metrics.steps++;
            metrics.comparisons++;
            if (data[i] == x) return true;
        }
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Metrics metrics() {
        return metrics;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException("index=" + index + ", size=" + size);
    }

    private void ensureCapacity() {
        if (size < data.length) return;
        int[] bigger = new int[data.length * 2];   // grow 2x
        for (int i = 0; i < size; i++) {
            bigger[i] = data[i];
            metrics.moves++;                       // every copied element is one move
        }
        data = bigger;
    }
}
