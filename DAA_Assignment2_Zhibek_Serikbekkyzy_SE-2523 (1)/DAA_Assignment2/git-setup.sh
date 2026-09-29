#!/usr/bin/env bash
# Recreates the required Git workflow from the assignment:
#   main (working code only, tag v1.0) + feature/metrics, feature/array, feature/list, feature/heap
# Run ONCE in the project root, with YOUR git identity configured (git config user.name / user.email).
# Then:  git remote add origin <your GitHub repo URL>  &&  git push -u origin --all  &&  git push origin v1.0
set -euo pipefail

[ -d .git ] || git init -b main
git checkout -B main >/dev/null 2>&1 || true

commit() { git add "${@:2}"; git commit -q -m "$1"; echo "  $1"; }
merge()  { git checkout -q main; git merge -q --no-ff "$1" -m "merge: $1 into main"; }

echo "main: project setup"
commit "chore: add maven project and .gitignore" pom.xml .gitignore

echo "feature/metrics: counters"
git checkout -q -b feature/metrics
commit "feat(metrics): add Metrics counters (steps, moves, comparisons)" src/main/java/daa/metrics/Metrics.java
commit "feat(metrics): add Result record and CsvWriter" src/main/java/daa/metrics/Result.java src/main/java/daa/metrics/CsvWriter.java
merge feature/metrics

echo "feature/array: DynamicArray"
git checkout -q -b feature/array
commit "feat(array): add IntList interface" src/main/java/daa/ds/IntList.java
commit "feat(array): add DynamicArray with 2x resizing and counters" src/main/java/daa/ds/DynamicArray.java
commit "feat(array): add FixedIntArray baseline" src/main/java/daa/ds/FixedIntArray.java
merge feature/array

echo "feature/list: MyLinkedList"
git checkout -q -b feature/list
commit "feat(list): add singly linked MyLinkedList with counters" src/main/java/daa/ds/MyLinkedList.java
commit "test(list): add contract tests for DynamicArray and MyLinkedList" src/test/java/daa/ds/IntListContractTest.java
merge feature/list

echo "feature/heap: MinHeap"
git checkout -q -b feature/heap
commit "feat(heap): add array-based MinHeap (insert, peekMin, extractMin, buildHeap)" src/main/java/daa/ds/MinHeap.java
commit "test(heap): check heap property and sorted output" src/test/java/daa/ds/MinHeapTest.java
merge feature/heap

echo "feature/metrics: benchmark"
git checkout -q feature/metrics
git merge -q main
commit "feat(metrics): add Benchmark with workloads W1-W4 and bonus B" src/main/java/daa/bench/Benchmark.java
commit "feat(metrics): add Main entry point" src/main/java/daa/Main.java
commit "test(metrics): check counters and CSV output" src/test/java/daa/metrics/MetricsTest.java
commit "feat(metrics): add results.csv and plot script" results/results.csv scripts/plot.py
merge feature/metrics

echo "main: documentation and release"
commit "docs(report): add plots" results/plots
commit "docs(report): add complexity table, loop invariants and discussion" REPORT.md
commit "docs(readme): add build, test and benchmark instructions" README.md git-setup.sh
git tag -a v1.0 -m "Assignment 2 release"
git checkout -q main
echo "Done. Branches:"; git branch
