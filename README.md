# DAA Assignment 2 - Data Structures (In-Memory Workload Engine)

`DynamicArray`, `MyLinkedList` and `MinHeap` implemented from scratch (primitive `int`, no `java.util` collections in `src/main`),
with operation counters, JUnit 5 tests, a benchmark of 4 workloads, and a report.

## Requirements
* JDK 17 or newer, Maven 3.8+
* Python 3 with `matplotlib` (only for the plots)

## Build and test
```bash
mvn clean test
```

## Run the benchmark (one command produces `results/results.csv`)
```bash
mvn compile exec:java
```
or run `daa.Main` from IntelliJ IDEA. Optionally pass another output path as the first argument.
The run takes about 15 seconds (sizes n = 100, 1 000, 10 000, 100 000; seed 42; 3 warm-up + 5 measured runs, median time).

## Build the plots
```bash
pip install matplotlib
python scripts/plot.py        # writes PNG charts to results/plots/
```

## Project layout
```
src/main/java/daa/ds        DynamicArray, MyLinkedList, MinHeap, FixedIntArray (baseline), IntList (common interface)
src/main/java/daa/metrics   Metrics (counters), Result, CsvWriter
src/main/java/daa/bench     Benchmark (workloads W1-W4 and bonus B)
src/main/java/daa/Main.java entry point
src/test/java/daa           JUnit 5 tests (correctness, edge cases, heap property, counters)
results/results.csv         benchmark output
results/plots/              PNG charts
REPORT.md                   complexity table, loop invariant proofs, plots, discussion
```

## What is counted
| Counter | Meaning |
|---|---|
| steps | one array-cell read/write, or one list-node visit |
| moves | one element shifted/copied in an array (also when the array grows), or one pointer update in the list |
| comparisons | one comparison of two elements |

Counters are incremented inside the methods. Only the measured workload is counted (filling the structure is not).

## CSV format
`workload,variant,structure,n,time_ms,steps,moves,comparisons` - `variant` is `head`/`middle` for W3 and `-` otherwise.
Extra rows: `FixedArray` (non-growing baseline) and `B_build_vs_insert` (bonus B: `buildHeap` vs n x `insert`).

## Git workflow
Branches `feature/metrics`, `feature/array`, `feature/list`, `feature/heap` are merged into `main`; the release is tagged `v1.0`.
`git-setup.sh` recreates this history with small commits (see comments inside).
