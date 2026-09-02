#!/usr/bin/env python3
# Copyright (c) 2026 Wind River Systems, Inc.
#
# Diff two snapshots produced by extract-config-vars.py and emit the pass/fail report described in
# docs/kas-to-fragments-migration-spec.md section 4.5. Exit code is 0 if every curated variable
# matches (modulo --ignore-vars), 1 otherwise.
#
# Usage:
#   scripts/validate-equivalence.py --cell NAME BASELINE.json CANDIDATE.json [--report-json FILE]
#       [--ignore-vars VAR1,VAR2,...]
#
# --ignore-vars: variables expected to differ and excluded from the pass/fail verdict, but still
#   listed in the report for visibility. Use for a documented, intentional difference -- eg
#   OE_FRAGMENTS when comparing a cell's pre-Phase-3 shape against its Phase 3 candidate, since
#   introducing OE_FRAGMENTS content is the entire point of that comparison, not a regression.
#
# SET_SEMANTIC_VARS below are compared as whitespace-tokenized sets, not exact strings: these are
# space-separated feature/class lists where bitbake itself only ever checks membership
# (bb.utils.contains, "in INHERIT", etc), never exact text or count, so a value appearing twice --
# which is expected and permanent for Phase 3 cells, since a fragment and the still-included kas
# local_conf_header that used to be the only source of the same setting both legitimately
# contribute it (spec section 5 Phase 3 design note) -- is not a real difference from a value
# appearing once. Comparing these as exact strings would make the tool permanently unable to pass
# a fragment-capable cell for a reason that has nothing to do with correctness.

import argparse
import json
from pathlib import Path

SET_SEMANTIC_VARS = {
    "DISTRO_FEATURES", "DISTRO_FEATURES_DEFAULTS", "DISTRO_FEATURES_OPTED_OUT",
    "IMAGE_FEATURES", "INHERIT", "INHERIT_DISTRO", "LICENSE_FLAGS_ACCEPTED",
    "WARN_QA", "ERROR_QA",
}

# BBLAYERS entries are absolute paths, and kas vs bitbake-setup (or two different KAS_WORK_DIRs)
# never share a checkout root, so exact string comparison can never pass even when the actual
# layer set is identical. Compare by each path's trailing "<layer-dir>" or "<repo>/<layer-dir>"
# component instead -- found live comparing a real kas checkout against a real bitbake-setup one
# for the same nominal cell, where every entry differed only in its checkout-root prefix.
PATH_SUFFIX_VARS = {"BBLAYERS"}


def bblayers_suffix(path):
    # ".../layers/meta-clang" -> "meta-clang"; ".../layers/meta-ros/meta-ros-common" ->
    # "meta-ros/meta-ros-common": keep at most the two components after the last "layers" segment,
    # since that is the part a layer's own identity/subdir is expressed in, on either tool.
    parts = path.split("/")
    if "layers" in parts:
        parts = parts[parts.index("layers") + 1:]
    return "/".join(parts[-2:]) if len(parts) > 1 else "/".join(parts)


def values_equal(name, b, c):
    if b == c:
        return True
    if not (b.get("defined") and c.get("defined")):
        return False
    if name in SET_SEMANTIC_VARS:
        return set(b.get("value", "").split()) == set(c.get("value", "").split())
    if name in PATH_SUFFIX_VARS:
        norm = lambda v: {bblayers_suffix(p) for p in v.split()}
        return norm(b.get("value", "")) == norm(c.get("value", ""))
    return False


def fmt(entry):
    if entry.get("defined") is False:
        return "<undefined>"
    if "error" in entry:
        return f"<error: {entry['error']}>"
    return entry.get("value")


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("baseline", type=Path)
    ap.add_argument("candidate", type=Path)
    ap.add_argument("--cell", required=True)
    ap.add_argument("--report-json", type=Path)
    ap.add_argument("--ignore-vars", default="", help="Comma-separated variable names excluded from the verdict")
    args = ap.parse_args()
    ignore_vars = {v.strip() for v in args.ignore_vars.split(",") if v.strip()}

    baseline = json.loads(args.baseline.read_text())
    candidate = json.loads(args.candidate.read_text())

    base_vars = baseline["variables"]
    cand_vars = candidate["variables"]
    all_names = sorted(set(base_vars) | set(cand_vars))

    diffs = []
    ignored_diffs = []
    for name in all_names:
        b = base_vars.get(name, {"error": "not captured in baseline"})
        c = cand_vars.get(name, {"error": "not captured in candidate"})
        if not values_equal(name, b, c):
            entry = {"variable": name, "baseline_value": fmt(b), "candidate_value": fmt(c)}
            (ignored_diffs if name in ignore_vars else diffs).append(entry)

    report = {
        "cell": args.cell,
        "baseline_kas_file": baseline.get("kas_file"),
        "candidate_kas_file": candidate.get("kas_file"),
        "pass": len(diffs) == 0,
        "variable_diffs": diffs,
        "ignored_diffs": ignored_diffs,
    }

    if args.report_json:
        args.report_json.write_text(json.dumps(report, indent=2, sort_keys=True) + "\n")

    if report["pass"]:
        suffix = f", {len(ignored_diffs)} ignored" if ignored_diffs else ""
        print(f"PASS: {args.cell} ({len(all_names)} variables checked{suffix}, baseline={baseline.get('kas_file')} candidate={candidate.get('kas_file')})")
        for d in ignored_diffs:
            print(f"  (ignored) {d['variable']}: baseline={d['baseline_value']!r} candidate={d['candidate_value']!r}")
    else:
        print(f"FAIL: {args.cell} ({len(diffs)}/{len(all_names)} variables differ)")
        for d in diffs:
            print(f"  {d['variable']}:")
            print(f"    baseline:  {d['baseline_value']}")
            print(f"    candidate: {d['candidate_value']}")

    raise SystemExit(0 if report["pass"] else 1)


if __name__ == "__main__":
    main()
