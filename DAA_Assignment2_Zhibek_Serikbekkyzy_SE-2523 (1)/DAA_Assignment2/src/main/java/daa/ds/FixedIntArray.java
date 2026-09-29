package daa.ds;

import daa.metrics.Metrics;

/**
 * Baseline: array with a FIXED capacity (never grows). Used in the benchmark only to show how much the
 * resizing costs. add() on a full array throws IllegalStateException.
 */
public class FixedIntArray implements IntList {
    private final int[] data;
    private int size;
    private final Metrics metrics = new Metrics();

    public FixedIntArray(int capacity) {
        data = new int[capacity];
    }

    @Override
    public void add(int x) {
        if (size == data.length) throw new IllegalStateException("array is full");
        data[size++] = x;
        metrics.steps++;
    }

    @Override
    public void add(int index, int x) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException("index=" + index + ", size=" + size);
        if (size == data.length) throw new IllegalStateException("array is full");
        for (int i = size; i > index; i--) {
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
        for (int i = index; i < size - 1; i++) {
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
}
