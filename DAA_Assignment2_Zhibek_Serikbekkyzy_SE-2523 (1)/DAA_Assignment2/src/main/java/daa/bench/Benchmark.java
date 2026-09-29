package daa.bench;

import daa.ds.DynamicArray;
import daa.ds.FixedIntArray;
import daa.ds.IntList;
import daa.ds.MinHeap;
import daa.ds.MyLinkedList;
import daa.metrics.Metrics;
import daa.metrics.Result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.IntFunction;

/**
 * Runs the 4 workloads of the assignment (+ bonus B) on sizes 100, 1 000, 10 000, 100 000.
 * Every case is run WARMUP + RUNS times on a fresh structure; the first WARMUP runs are thrown away,
 * the reported time is the MEDIAN of the RUNS measured runs. Data always comes from new Random(42).
 */
public final class Benchmark {
    public static final int[] SIZES = {100, 1_000, 10_000, 100_000};
    public static final int SEED = 42;
    public static final int WARMUP = 3;
    public static final int RUNS = 5;

    static final int W1_OPS = 10_000;
    static final int W2_QUERIES = 1_000;
    static final int W3_OPS = 1_000;

    /** Prevents the JIT from removing the benchmarked code (dead-code elimination). */
    private static volatile long sink;

    private interface Op {
        void apply(IntList list);
    }

    private record Impl(String name, IntFunction<IntList> factory) {}

    private static final List<Impl> IMPLS = List.of(
            new Impl("DynamicArray", n -> new DynamicArray()),
            new Impl("MyLinkedList", n -> new MyLinkedList()),
            new Impl("FixedArray", n -> new FixedIntArray(n + W3_OPS)));   // baseline, never grows

    private Benchmark() {}

    /** Runs everything and returns all rows of results.csv. */
    public static List<Result> runAll(boolean verbose) {
        runSuite(new int[]{1_000}, false);          // global JVM warm-up, results discarded
        return runSuite(SIZES, verbose);
    }

    private static List<Result> runSuite(int[] sizes, boolean verbose) {
        List<Result> rows = new ArrayList<>();
        for (int n : sizes) {
            Random rnd = new Random(SEED);
            int[] data = new int[n];
            for (int i = 0; i < n; i++) data[i] = rnd.nextInt(1_000_000_000);   // values are >= 0

            // ---- W1: random access -------------------------------------------------------------
            int[] indices = new int[W1_OPS];
            for (int i = 0; i < W1_OPS; i++) indices[i] = rnd.nextInt(n);
            Op w1 = list -> {
                long s = 0;
                for (int idx : indices) s += list.get(idx);
                sink = s;
            };
            for (Impl impl : IMPLS) add(rows, verbose, measure("W1_random_access", "-", impl, n, data, w1));

            // ---- W2: search (half present, half absent: absent values are negative) -------------
            int[] queries = new int[W2_QUERIES];
            for (int i = 0; i < W2_QUERIES; i++) queries[i] = (i % 2 == 0) ? data[rnd.nextInt(n)] : -1 - i;
            Op w2 = list -> {
                int found = 0;
                for (int q : queries) if (list.contains(q)) found++;
                if (found != W2_QUERIES / 2) throw new IllegalStateException("W2: wrong number of hits " + found);
                sink = found;
            };
            for (Impl impl : IMPLS) add(rows, verbose, measure("W2_search", "-", impl, n, data, w2));

            // ---- W3: insert & remove at head / middle -----------------------------------------
            for (String variant : new String[]{"head", "middle"}) {
                final int pos = variant.equals("head") ? 0 : n / 2;
                Op w3 = list -> {
                    for (int i = 0; i < W3_OPS; i++) list.add(pos, i);
                    long s = 0;
                    for (int i = 0; i < W3_OPS; i++) s += list.remove(pos);
                    sink = s;
                };
                for (Impl impl : IMPLS) add(rows, verbose, measure("W3_insert_remove", variant, impl, n, data, w3));
            }

            // ---- W4: priority processing (MinHeap) --------------------------------------------
            add(rows, verbose, measureHeap("W4_priority", "-", "MinHeap", n, data, false));

            // ---- Bonus B: buildHeap (O(n)) vs n x insert (O(n log n)) -------------------------
            add(rows, verbose, measureHeap("B_build_vs_insert", "insert", "MinHeap", n, data, false, true));
            add(rows, verbose, measureHeap("B_build_vs_insert", "buildHeap", "MinHeap", n, data, true, true));
        }
        return rows;
    }

    private static void add(List<Result> rows, boolean verbose, Result r) {
        rows.add(r);
        if (verbose) System.out.println(r.toCsvLine());
    }

    /** Fill (not measured) -> reset counters -> run the workload (measured). */
    private static Result measure(String workload, String variant, Impl impl, int n, int[] data, Op op) {
        double[] times = new double[RUNS];
        Metrics last = null;
        for (int iter = 0; iter < WARMUP + RUNS; iter++) {
            IntList list = impl.factory().apply(n);
            for (int v : data) list.add(v);
            list.metrics().reset();

            long t0 = System.nanoTime();
            op.apply(list);
            long elapsed = System.nanoTime() - t0;

            if (iter >= WARMUP) times[iter - WARMUP] = elapsed / 1e6;
            last = list.metrics();
        }
        return new Result(workload, variant, impl.name(), n, median(times), last.steps, last.moves, last.comparisons);
    }

    private static Result measureHeap(String workload, String variant, String name, int n, int[] data, boolean build) {
        return measureHeap(workload, variant, name, n, data, build, false);
    }

    /**
     * W4 (insertOnly=false): insert n values, then extractMin n times and verify non-decreasing order.
     * Bonus B (insertOnly=true): only the construction phase, either n x insert or one buildHeap call.
     */
    private static Result measureHeap(String workload, String variant, String name, int n, int[] data,
                                      boolean build, boolean insertOnly) {
        double[] times = new double[RUNS];
        Metrics last = null;
        for (int iter = 0; iter < WARMUP + RUNS; iter++) {
            MinHeap heap = new MinHeap();
            heap.metrics().reset();

            long t0 = System.nanoTime();
            if (build) {
                heap.buildHeap(data);
            } else {
                for (int v : data) heap.insert(v);
            }
            if (!insertOnly) {
                int prev = Integer.MIN_VALUE;
                for (int i = 0; i < n; i++) {
                    int v = heap.extractMin();
                    if (v < prev) throw new IllegalStateException("W4: output is not sorted");
                    prev = v;
                }
                sink = prev;
            }
            long elapsed = System.nanoTime() - t0;

            if (iter >= WARMUP) times[iter - WARMUP] = elapsed / 1e6;
            last = heap.metrics();
        }
        return new Result(workload, variant, name, n, median(times), last.steps, last.moves, last.comparisons);
    }

    private static double median(double[] values) {
        double[] copy = values.clone();
        Arrays.sort(copy);
        return copy[copy.length / 2];
    }
}
