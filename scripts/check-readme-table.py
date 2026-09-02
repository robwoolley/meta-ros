#!/usr/bin/env python3
# Copyright (c) 2026 Wind River Systems, Inc.
#
# Verify kas/README.md's "Supported combinations" table, between its
# "<!-- Generated from matrix.yml -->" / "<!-- End generated table -->" markers, matches what
# scripts/generate-matrix-table.py would emit from matrix.yml right now. Companion to
# generate-kas.py --check (spec section 3.3): that catches drift in the generated kas/*.yml
# files, this catches the same kind of drift in the one part of the README that mirrors them.
#
# Usage: scripts/check-readme-table.py [--matrix PATH] [--readme PATH]

import argparse
import subprocess
import sys
from pathlib import Path

START_MARKER = "<!-- Generated from matrix.yml"
END_MARKER = "<!-- End generated table -->"


def extract_current_table(readme_text):
    lines = readme_text.splitlines()
    start = next((i for i, l in enumerate(lines) if l.startswith(START_MARKER)), None)
    end = next((i for i, l in enumerate(lines) if l.startswith(END_MARKER)), None)
    if start is None or end is None or end <= start:
        raise SystemExit(f"Could not find {START_MARKER!r} / {END_MARKER!r} markers in README")
    return "\n".join(lines[start + 1:end]) + "\n"


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--matrix", default="matrix.yml", type=Path)
    ap.add_argument("--readme", default="kas/README.md", type=Path)
    args = ap.parse_args()

    script_dir = Path(__file__).resolve().parent
    result = subprocess.run(
        [sys.executable, str(script_dir / "generate-matrix-table.py"), "--matrix", str(args.matrix)],
        capture_output=True, text=True,
    )
    if result.returncode != 0:
        sys.stderr.write(result.stderr)
        raise SystemExit(result.returncode)
    expected = result.stdout

    current = extract_current_table(args.readme.read_text())

    if current != expected:
        print(f"DRIFT: {args.readme} table does not match scripts/generate-matrix-table.py output")
        print("--- current README table ---")
        print(current)
        print("--- expected (from matrix.yml) ---")
        print(expected)
        sys.exit(1)

    print(f"{args.readme} table matches matrix.yml")


if __name__ == "__main__":
    main()
