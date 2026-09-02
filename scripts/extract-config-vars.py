#!/usr/bin/env python3
# Copyright (c) 2026 Wind River Systems, Inc.
#
# Tier 2 of the validation framework (docs/kas-to-fragments-migration-spec.md section 4): checks
# out a kas top-level file for real (no build) and extracts the curated config-level variable set
# via bitbake-getvar, emitting a JSON snapshot. Two snapshots for the same cell -- eg the current
# kas-file shape and a Phase 3 reduced/fragment-based shape -- can then be diffed with
# validate-equivalence.py to confirm they resolve to the same bitbake configuration.
#
# This intentionally covers only config-level variables (bitbake-getvar without -r <recipe>), per
# the "old kas shape vs new kas shape" equivalence Phase 3 needs. Recipe-level content (bbappends
# for zenoh-c, python3, etc.) does not change between kas shapes and is out of scope here; it would
# matter for a kas-vs-bitbake-setup cross-tool check instead (Phase 4), not this one.
#
# Usage:
#   scripts/extract-config-vars.py KAS_FILE --work-dir DIR --kas-bin PATH --bitbake-getvar-bin PATH
#       [--out FILE]
#
# --work-dir should be reused across cells (a shared KAS_WORK_DIR) so repos already fetched for one
# cell are not re-cloned for the next, per the spec's runtime-budget design (section 4.3/4.4).

import argparse
import json
import os
import subprocess
import sys
from pathlib import Path

CURATED_VARS = [
    "MACHINE", "DISTRO", "DISTRO_FEATURES", "DISTRO_FEATURES_DEFAULTS", "DISTRO_FEATURES_OPTED_OUT",
    "IMAGE_FEATURES", "INHERIT", "INHERIT_DISTRO", "BBLAYERS", "OE_FRAGMENTS",
    "LICENSE_FLAGS_ACCEPTED", "TEMPLATECONF", "WARN_QA", "ERROR_QA",
    "VIRTUAL-RUNTIME_init_manager",
    "BB_DISKMON_DIRS", "BB_NICE_LEVEL", "BB_PRESSURE_MAX_CPU", "BB_PRESSURE_MAX_IO", "BB_PRESSURE_MAX_MEMORY",
]


def kas_checkout(kas_bin, kas_file, work_dir):
    env = os.environ.copy()
    env["KAS_WORK_DIR"] = str(work_dir)
    result = subprocess.run([kas_bin, "checkout", str(kas_file)], capture_output=True, text=True, env=env)
    if result.returncode != 0:
        print(result.stdout, file=sys.stderr)
        print(result.stderr, file=sys.stderr)
        raise SystemExit(f"kas checkout failed for {kas_file}")
    return result


def find_build_dir(work_dir):
    # kas checkout creates <work_dir>/build by default.
    build_dir = work_dir / "build"
    if not (build_dir / "conf" / "local.conf").exists():
        raise SystemExit(f"no conf/local.conf found under {build_dir}")
    return build_dir


def getvar(bitbake_getvar_bin, build_dir, varname):
    full_env = os.environ.copy()
    full_env["BBPATH"] = str(build_dir)
    # --value: bare value, no "VAR=" prefix and no commented history block, so output is
    # unambiguous to parse regardless of what the value itself contains.
    # --ignore-undefined: report an empty value instead of erroring for a variable nothing sets
    # (eg BB_DISKMON_DIRS on a cell that never includes kas/diskmon.yml) -- we still want the
    # snapshot to record "unset" distinctly from a real empty string, so this is combined with a
    # separate --value-less probe below rather than trusted blindly.
    probe = subprocess.run(
        [bitbake_getvar_bin, varname],
        capture_output=True, text=True, cwd=build_dir, env=full_env,
    )
    if probe.returncode != 0 and "is not defined" in probe.stdout + probe.stderr:
        return {"value": None, "defined": False}

    result = subprocess.run(
        [bitbake_getvar_bin, "--value", varname],
        capture_output=True, text=True, cwd=build_dir, env=full_env,
    )
    if result.returncode != 0:
        return {"error": (result.stdout + result.stderr).strip()[-2000:]}
    return {"value": result.stdout.rstrip("\n"), "defined": True}


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("kas_file", type=Path)
    ap.add_argument("--work-dir", required=True, type=Path)
    ap.add_argument("--kas-bin", default="kas")
    ap.add_argument("--bitbake-getvar-bin", default="bitbake-getvar")
    ap.add_argument("--out", type=Path)
    ap.add_argument("--skip-checkout", action="store_true",
                     help="Reuse an already-checked-out build dir under --work-dir instead of running kas checkout again")
    args = ap.parse_args()

    args.work_dir.mkdir(parents=True, exist_ok=True)

    if not args.skip_checkout:
        kas_checkout(args.kas_bin, args.kas_file, args.work_dir)

    build_dir = find_build_dir(args.work_dir)

    snapshot = {"kas_file": str(args.kas_file), "variables": {}}
    for var in CURATED_VARS:
        snapshot["variables"][var] = getvar(args.bitbake_getvar_bin, build_dir, var)

    out_text = json.dumps(snapshot, indent=2, sort_keys=True)
    if args.out:
        args.out.write_text(out_text + "\n")
        print(f"Wrote {args.out}")
    else:
        print(out_text)


if __name__ == "__main__":
    main()
