package daa.ds;

import daa.metrics.Metrics;

/** Common interface of DynamicArray, FixedIntArray and MyLinkedList, so ONE benchmark runs all of them. */
public interface IntList {
    /** Append x to the end. */
    void add(int x);

    /** Insert x at position index (0..size). @throws IndexOutOfBoundsException if index is invalid. */
    void add(int index, int x);

    /** Remove and return the element at index (0..size-1). @throws IndexOutOfBoundsException if invalid. */
    int remove(int index);

    /** @throws IndexOutOfBoundsException if index is invalid. */
    int get(int index);

    boolean contains(int x);

    int size();

    Metrics metrics();
}
