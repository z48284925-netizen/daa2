package daa.metrics;

/** One row of results.csv. */
public record Result(String workload, String variant, String structure, int n,
                     double timeMs, long steps, long moves, long comparisons) {
    public static final String HEADER = "workload,variant,structure,n,time_ms,steps,moves,comparisons";

    public String toCsvLine() {
        return String.format(java.util.Locale.ROOT, "%s,%s,%s,%d,%.3f,%d,%d,%d",
                workload, variant, structure, n, timeMs, steps, moves, comparisons);
    }
}
