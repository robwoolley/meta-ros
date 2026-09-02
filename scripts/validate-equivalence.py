#!/usr/bin/env python3
# Copyright (c) 2026 Wind River Systems, Inc.
#
# Diff two snapshots produced by extract-config-vars.py and emit the pass/fail report described in
# docs/kas-to-fragments-migration-spec.md section 4.5. Exit code is 0 if every curated variable
# matches, 1 otherwise.
#
# Usage:
#   scripts/validate-equivalence.py --cell NAME BASELINE.json CANDIDATE.json [--report-json FILE]

import argparse
import json
from pathlib import Path


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("baseline", type=Path)
    ap.add_argument("candidate", type=Path)
    ap.add_argument("--cell", required=True)
    ap.add_argument("--report-json", type=Path)
    args = ap.parse_args()

    baseline = json.loads(args.baseline.read_text())
    candidate = json.loads(args.candidate.read_text())

    base_vars = baseline["variables"]
    cand_vars = candidate["variables"]
    all_names = sorted(set(base_vars) | set(cand_vars))

    diffs = []
    for name in all_names:
        b = base_vars.get(name, {"error": "not captured in baseline"})
        c = cand_vars.get(name, {"error": "not captured in candidate"})
        if b != c:
            diffs.append({
                "variable": name,
                "baseline_value": b.get("value", f"<{b.get('error')}>"),
                "candidate_value": c.get("value", f"<{c.get('error')}>"),
            })

    report = {
        "cell": args.cell,
        "baseline_kas_file": baseline.get("kas_file"),
        "candidate_kas_file": candidate.get("kas_file"),
        "pass": len(diffs) == 0,
        "variable_diffs": diffs,
    }

    if args.report_json:
        args.report_json.write_text(json.dumps(report, indent=2, sort_keys=True) + "\n")

    if report["pass"]:
        print(f"PASS: {args.cell} ({len(all_names)} variables checked, baseline={baseline.get('kas_file')} candidate={candidate.get('kas_file')})")
    else:
        print(f"FAIL: {args.cell} ({len(diffs)}/{len(all_names)} variables differ)")
        for d in diffs:
            print(f"  {d['variable']}:")
            print(f"    baseline:  {d['baseline_value']}")
            print(f"    candidate: {d['candidate_value']}")

    raise SystemExit(0 if report["pass"] else 1)


if __name__ == "__main__":
    main()
