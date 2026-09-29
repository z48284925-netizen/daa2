package daa.ds;

import daa.metrics.Metrics;

/**
 * Singly linked list of primitive ints with head and tail pointers
 * (own class, named MyLinkedList so it does not clash with java.util.LinkedList).
 *
 * Counting rules: every node that is visited while walking = 1 step;
 * every pointer assignment (head, tail, node.next) = 1 move.
 */
public class MyLinkedList implements IntList {

    private static final class Node {
        final int value;
        Node next;

        Node(int value) {
            this.value = value;
        }
    }

    private Node head;
    private Node tail;
    private int size;
    private final Metrics metrics = new Metrics();

    @Override
    public void add(int x) {
        Node node = new Node(x);
        if (head == null) {
            head = node;
            tail = node;
            metrics.moves += 2;         // head, tail
        } else {
            tail.next = node;
            tail = node;
            metrics.moves += 2;         // old tail.next, tail
        }
        size++;
    }

    @Override
    public void add(int index, int x) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException("index=" + index + ", size=" + size);
        if (index == size) {
            add(x);
            return;
        }
        Node node = new Node(x);
        if (index == 0) {
            node.next = head;
            head = node;
            metrics.moves += 2;         // node.next, head
        } else {
            Node prev = nodeAt(index - 1);
            node.next = prev.next;
            prev.next = node;
            metrics.moves += 2;         // node.next, prev.next
        }
        size++;
    }

    @Override
    public int remove(int index) {
        checkIndex(index);
        Node removed;
        if (index == 0) {
            removed = head;
            metrics.steps++;
            head = head.next;
            metrics.moves++;
            if (head == null) {
                tail = null;
                metrics.moves++;
            }
        } else {
            Node prev = nodeAt(index - 1);
            removed = prev.next;
            metrics.steps++;            // visit the removed node
            prev.next = removed.next;
            metrics.moves++;
            if (removed == tail) {
                tail = prev;
                metrics.moves++;
            }
        }
        size--;
        return removed.value;
    }

    @Override
    public int get(int index) {
        checkIndex(index);
        return nodeAt(index).value;
    }

    /**
     * Linear search.
     * Loop invariant: before each iteration, x does not occur in the nodes already passed
     * (all nodes from head up to, but not including, cur).
     */
    @Override
    public boolean contains(int x) {
        for (Node cur = head; cur != null; cur = cur.next) {
            metrics.steps++;
            metrics.comparisons++;
            if (cur.value == x) return true;
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

    /** Walk from head to the node at index; visits index+1 nodes. Assumes 0 <= index < size. */
    private Node nodeAt(int index) {
        Node cur = head;
        metrics.steps++;                // visit head
        for (int i = 0; i < index; i++) {
            cur = cur.next;
            metrics.steps++;            // move to next node
        }
        return cur;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException("index=" + index + ", size=" + size);
    }
}
