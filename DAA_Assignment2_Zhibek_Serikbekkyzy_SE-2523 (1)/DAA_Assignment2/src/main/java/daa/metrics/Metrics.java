package daa.metrics;

/**
 * Counters of physical operations. Every data structure increments them
 * INSIDE its own methods, so nothing is estimated afterwards.
 *
 * steps       - one read of an array cell OR one visit/move to a list node
 * moves       - one element shifted/copied inside an array OR one link (pointer) update in a list
 * comparisons - one comparison of two elements
 */
public final class Metrics {
    public long steps;
    public long moves;
    public long comparisons;

    public void reset() {
        steps = 0;
        moves = 0;
        comparisons = 0;
    }

    @Override
    public String toString() {
        return "steps=" + steps + ", moves=" + moves + ", comparisons=" + comparisons;
    }
}
