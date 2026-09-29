package daa.ds;

import daa.metrics.Metrics;

/**
 * Array-based binary min-heap of primitive ints.
 * Parent of i is (i-1)/2, children are 2i+1 and 2i+2.
 * Heap property: a[parent] <= a[child] for every non-root node.
 */
public class MinHeap {
    private int[] a;
    private int size;
    private final Metrics metrics = new Metrics();

    public MinHeap() {
        this(16);
    }

    public MinHeap(int initialCapacity) {
        a = new int[Math.max(1, initialCapacity)];
    }

    /** O(log n) worst case, O(1) average for random data. */
    public void insert(int x) {
        if (size == a.length) grow();
        a[size] = x;
        metrics.steps++;
        siftUp(size);
        size++;
    }

    /** O(1). @throws IllegalStateException if the heap is empty. */
    public int peekMin() {
        if (size == 0) throw new IllegalStateException("heap is empty");
        metrics.steps++;
        return a[0];
    }

    /** O(log n). @throws IllegalStateException if the heap is empty. */
    public int extractMin() {
        if (size == 0) throw new IllegalStateException("heap is empty");
        int min = a[0];
        metrics.steps++;
        size--;
        if (size > 0) {
            a[0] = a[size];
            metrics.steps++;
            metrics.moves++;
            siftDown(0);
        }
        return min;
    }

    /**
     * Bonus B: Floyd's bottom-up heap construction in O(n).
     * Replaces the heap content with a copy of the given array and heapifies it.
     */
    public void buildHeap(int[] array) {
        a = new int[Math.max(1, array.length)];
        for (int i = 0; i < array.length; i++) {
            a[i] = array[i];
            metrics.moves++;
        }
        size = array.length;
        for (int i = size / 2 - 1; i >= 0; i--) {
            siftDown(i);
        }
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Metrics metrics() {
        return metrics;
    }

    /** Used by tests: checks a[parent] <= a[child] for all nodes. */
    public boolean isValidHeap() {
        for (int child = 1; child < size; child++) {
            if (a[(child - 1) / 2] > a[child]) return false;
        }
        return true;
    }

    /** Bubble-up. */
    private void siftUp(int i) {
        int x = a[i];
        while (i > 0) {
            int parent = (i - 1) / 2;
            metrics.comparisons++;
            metrics.steps++;                 // read a[parent]
            if (a[parent] <= x) break;
            a[i] = a[parent];                // move the parent down
            metrics.moves++;
            i = parent;
        }
        a[i] = x;
        metrics.moves++;
    }

    /**
     * Bubble-down.
     * Loop invariant (see REPORT.md): before each iteration the subtree rooted at every node except i is a heap,
     * x is the value being sunk (a[i] holds a "hole" copy of x), and a[parent(i)] <= both children of i.
     */
    private void siftDown(int i) {
        int x = a[i];
        metrics.steps++;
        while (true) {
            int left = 2 * i + 1;
            if (left >= size) break;
            int smallest = left;
            int right = left + 1;
            if (right < size) {
                metrics.comparisons++;
                metrics.steps += 2;          // read both children
                if (a[right] < a[left]) smallest = right;
            } else {
                metrics.steps++;
            }
            metrics.comparisons++;
            if (x <= a[smallest]) break;
            a[i] = a[smallest];              // move the smaller child up
            metrics.moves++;
            i = smallest;
        }
        a[i] = x;
        metrics.moves++;
    }

    private void grow() {
        int[] bigger = new int[a.length * 2];
        for (int i = 0; i < size; i++) {
            bigger[i] = a[i];
            metrics.moves++;
        }
        a = bigger;
    }
}
