package daa;

import daa.bench.Benchmark;
import daa.metrics.CsvWriter;
import daa.metrics.Result;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** Entry point: runs the whole benchmark and writes results/results.csv (or the path given as argument). */
public class Main {
    public static void main(String[] args) throws IOException {
        Path out = args.length > 0 ? Path.of(args[0]) : Path.of("results", "results.csv");
        System.out.println("Running benchmark (seed=" + Benchmark.SEED + ", warm-up=" + Benchmark.WARMUP
                + ", runs=" + Benchmark.RUNS + ") ...");
        System.out.println(Result.HEADER);
        List<Result> rows = Benchmark.runAll(true);
        CsvWriter.write(out, rows);
        System.out.println("Saved " + rows.size() + " rows to " + out.toAbsolutePath());
    }
}
