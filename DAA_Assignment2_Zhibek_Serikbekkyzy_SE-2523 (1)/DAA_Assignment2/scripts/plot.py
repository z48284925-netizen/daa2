#!/usr/bin/env python3
"""Builds the PNG charts in results/plots/ from results/results.csv.
Usage:  pip install matplotlib   &&   python scripts/plot.py
"""
import csv
import os
import sys

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CSV_PATH = os.path.join(ROOT, "results", "results.csv")
OUT_DIR = os.path.join(ROOT, "results", "plots")
os.makedirs(OUT_DIR, exist_ok=True)

rows = list(csv.DictReader(open(CSV_PATH, encoding="utf-8")))
for r in rows:
    r["n"] = int(r["n"])
    for k in ("time_ms", "steps", "moves", "comparisons"):
        r[k] = float(r[k])

STYLE = {
    "DynamicArray": dict(color="tab:blue", marker="o", linestyle="-"),
    "MyLinkedList": dict(color="tab:red", marker="s", linestyle="-"),
    "FixedArray": dict(color="gray", marker="^", linestyle="--", alpha=0.7),
    "MinHeap": dict(color="tab:green", marker="D", linestyle="-"),
}
METRICS = [("steps", "Steps (array reads / node visits)"),
           ("moves", "Moves (shifts / pointer updates)"),
           ("comparisons", "Comparisons")]


def series(workload, variant, structure, key):
    pts = sorted((r["n"], r[key]) for r in rows
                 if r["workload"] == workload and r["variant"] == variant and r["structure"] == structure)
    return [p[0] for p in pts], [p[1] for p in pts]


def structures_of(workload):
    seen = []
    for r in rows:
        if r["workload"] == workload and r["structure"] not in seen:
            seen.append(r["structure"])
    return seen


def time_chart(workload, title, variants, fname):
    fig, axes = plt.subplots(1, len(variants), figsize=(6.2 * len(variants), 4.6), squeeze=False)
    for ax, variant in zip(axes[0], variants):
        for s in structures_of(workload):
            x, y = series(workload, variant, s, "time_ms")
            ax.plot(x, y, label=s, **STYLE[s])
        ax.set_xscale("log")
        ax.set_yscale("log")
        ax.set_xlabel("n (number of elements)")
        ax.set_ylabel("Median time (ms)")
        ax.set_title(title + ("" if variant == "-" else f" - {variant}"))
        ax.grid(True, which="both", alpha=0.3)
        ax.legend()
    fig.tight_layout()
    fig.savefig(os.path.join(OUT_DIR, fname), dpi=150)
    plt.close(fig)


def ops_chart(workload, title, variants, fname):
    fig, axes = plt.subplots(len(variants), 3, figsize=(16, 4.3 * len(variants)), squeeze=False)
    for vi, variant in enumerate(variants):
        for mi, (key, label) in enumerate(METRICS):
            ax = axes[vi][mi]
            for s in structures_of(workload):
                x, y = series(workload, variant, s, key)
                ax.plot(x, y, label=s, **STYLE[s])
            ax.set_xscale("log")
            if all(v == 0 for s2 in structures_of(workload) for v in series(workload, variant, s2, key)[1]):
                ax.set_ylim(-1, 1)                 # counter is 0 for every structure
                ax.text(0.5, 0.6, "0 for all structures", transform=ax.transAxes, ha="center")
            else:
                ax.set_yscale("symlog", linthresh=1)   # symlog: zero counters stay visible
                ax.set_ylim(bottom=0)
            ax.set_xlabel("n (number of elements)")
            ax.set_ylabel("Count (operations)")
            ax.set_title(label + ("" if variant == "-" else f" [{variant}]"), fontsize=10)
            ax.grid(True, which="both", alpha=0.3)
            ax.legend(fontsize=8)
    fig.suptitle(title, fontsize=12)
    fig.tight_layout()
    fig.savefig(os.path.join(OUT_DIR, fname), dpi=150)
    plt.close(fig)


time_chart("W1_random_access", "W1 random access: time vs n", ["-"], "W1_time.png")
ops_chart("W1_random_access", "W1 random access: steps / moves / comparisons vs n", ["-"], "W1_ops.png")
time_chart("W2_search", "W2 search: time vs n", ["-"], "W2_time.png")
ops_chart("W2_search", "W2 search: steps / moves / comparisons vs n", ["-"], "W2_ops.png")
time_chart("W3_insert_remove", "W3 insert & remove: time vs n", ["head", "middle"], "W3_time.png")
ops_chart("W3_insert_remove", "W3 insert & remove: steps / moves / comparisons vs n", ["head", "middle"], "W3_ops.png")

# W4: MinHeap only
fig, ax = plt.subplots(figsize=(6.2, 4.6))
x, y = series("W4_priority", "-", "MinHeap", "time_ms")
ax.plot(x, y, label="MinHeap (n x insert + n x extractMin)", **STYLE["MinHeap"])
ax.set_xscale("log"); ax.set_yscale("log")
ax.set_xlabel("n (number of elements)"); ax.set_ylabel("Median time (ms)")
ax.set_title("W4 priority processing: time vs n"); ax.grid(True, which="both", alpha=0.3); ax.legend()
fig.tight_layout(); fig.savefig(os.path.join(OUT_DIR, "W4_time.png"), dpi=150); plt.close(fig)

fig, ax = plt.subplots(figsize=(6.2, 4.6))
for key, label in METRICS:
    x, y = series("W4_priority", "-", "MinHeap", key)
    ax.plot(x, y, marker="o", label=label)
ax.set_xscale("log"); ax.set_yscale("log")
ax.set_xlabel("n (number of elements)"); ax.set_ylabel("Count (operations)")
ax.set_title("W4 priority processing: steps / moves / comparisons vs n")
ax.grid(True, which="both", alpha=0.3); ax.legend(fontsize=8)
fig.tight_layout(); fig.savefig(os.path.join(OUT_DIR, "W4_ops.png"), dpi=150); plt.close(fig)

# Bonus B: buildHeap vs n x insert
fig, axes = plt.subplots(1, 2, figsize=(12, 4.6))
for variant, style in (("insert", "o-"), ("buildHeap", "s--")):
    x, y = series("B_build_vs_insert", variant, "MinHeap", "time_ms")
    axes[0].plot(x, y, style, label=("n x insert(x)" if variant == "insert" else "buildHeap (Floyd)"))
    x, y = series("B_build_vs_insert", variant, "MinHeap", "comparisons")
    axes[1].plot(x, y, style, label=("n x insert(x)" if variant == "insert" else "buildHeap (Floyd)"))
for ax, yl in zip(axes, ("Median time (ms)", "Comparisons (count)")):
    ax.set_xscale("log"); ax.set_yscale("log")
    ax.set_xlabel("n (number of elements)"); ax.set_ylabel(yl)
    ax.grid(True, which="both", alpha=0.3); ax.legend()
axes[0].set_title("Bonus B: heap construction, time")
axes[1].set_title("Bonus B: heap construction, comparisons")
fig.tight_layout(); fig.savefig(os.path.join(OUT_DIR, "B_buildheap.png"), dpi=150); plt.close(fig)
print("plots saved to", OUT_DIR)
